package com.telesekreter.app.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.data.local.entity.PersonEntity
import com.telesekreter.app.domain.usecase.VariableParserUseCase
import com.telesekreter.app.util.SmsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_APPROVE_SEND = "com.telesekreter.app.ACTION_APPROVE_SEND"
        const val ACTION_CANCEL_TASK = "com.telesekreter.app.ACTION_CANCEL_TASK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)
        val taskType = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TYPE) ?: TaskAlarmScheduler.TYPE_MESSAGE

        if (taskId == -1L) return

        val database = AppDatabase.getDatabase(context)

        // Bildirimi kapat
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        notificationManager?.cancel(taskId.toInt())

        when (intent.action) {
            ACTION_APPROVE_SEND -> {
                CoroutineScope(Dispatchers.IO).launch {
                    sendDirectMessage(context, database, taskId)
                }
                return
            }
            ACTION_CANCEL_TASK -> {
                CoroutineScope(Dispatchers.IO).launch {
                    database.scheduledMessageDao().updateStatus(taskId, "IPTAL_EDILDI", "Kullanıcı bildirimden iptal etti.")
                }
                return
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (taskType) {
                TaskAlarmScheduler.TYPE_MESSAGE -> {
                    handleScheduledMessage(context, database, taskId)
                }
                TaskAlarmScheduler.TYPE_REMINDER -> {
                    handleReminder(context, database, taskId)
                }
                TaskAlarmScheduler.TYPE_CALL -> {
                    handleCallTask(context, database, taskId)
                }
            }
        }
    }

    private suspend fun handleScheduledMessage(context: Context, db: AppDatabase, messageId: Long) {
        val msg = db.scheduledMessageDao().getMessageById(messageId) ?: return

        val personIds = msg.recipientPersonIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        val recipients = db.personDao().getPersonsByIds(personIds)

        if (msg.channel == "WHATSAPP") {
            // WhatsApp bildirimini tek tıkla doğrudan WhatsApp'a gitmek üzere hazırla
            val firstPerson = recipients.firstOrNull()
            val parsedText = if (firstPerson != null) VariableParserUseCase.parse(msg.messageText, firstPerson) else msg.messageText
            TaskNotificationManager.showMessageNotification(
                context,
                messageId.toInt(),
                "💬 WhatsApp Mesaj Zamanı: ${msg.recipientSummary}",
                "Mesaj: $parsedText\n(Dokunup WhatsApp'tan gönderin)"
            )
            if (firstPerson != null) {
                SmsUtils.openWhatsApp(context, firstPerson.phone, parsedText)
            }
            db.scheduledMessageDao().updateStatus(messageId, "BASARILI", "WhatsApp mesaj ekranı açıldı.")
        } else if (msg.requireConfirmation) {
            // Güvenli mod: Gönderim öncesi onay bildirimi göster + Doğrudan Butonlar
            TaskNotificationManager.showMessageNotification(
                context,
                messageId.toInt(),
                "⏳ Mesaj Onayı Bekleniyor (${msg.recipientSummary})",
                "Mesaj: ${msg.messageText}\n\nBildirimdeki 'ONAYLA & GÖNDER' butonuna veya uygulamadaki 'Onay Bekleyenler' sekmesine dokunun.",
                isPendingApproval = true,
                messageId = messageId
            )
            db.scheduledMessageDao().updateStatus(messageId, "ONAY_BEKLIYOR", "Kullanıcı onayı bekleniyor.")
        } else {
            // Sormadan Otomatik Gönderim Modu (SMS)
            sendDirectMessage(context, db, messageId)
        }
    }

    private suspend fun sendDirectMessage(context: Context, db: AppDatabase, messageId: Long) {
        val msg = db.scheduledMessageDao().getMessageById(messageId) ?: return
        val personIds = msg.recipientPersonIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        val recipients = db.personDao().getPersonsByIds(personIds)

        if (msg.channel == "WHATSAPP") {
            val firstPerson = recipients.firstOrNull()
            if (firstPerson != null) {
                val parsedText = VariableParserUseCase.parse(msg.messageText, firstPerson)
                SmsUtils.openWhatsApp(context, firstPerson.phone, parsedText)
            }
            db.scheduledMessageDao().updateStatus(messageId, "BASARILI", "WhatsApp mesaj ekranı açıldı.")
            return
        }

        var successCount = 0
        var failCount = 0
        val errorDetails = mutableListOf<String>()

        for (person in recipients) {
            val parsedText = VariableParserUseCase.parse(msg.messageText, person)
            val (sent, errorMsg) = SmsUtils.sendSmsDirect(context, person.phone, parsedText)
            if (sent) {
                successCount++
            } else {
                failCount++
                errorDetails.add("${person.fullName}: $errorMsg")
            }
        }

        if (failCount == 0 && successCount > 0) {
            db.scheduledMessageDao().updateStatus(messageId, "BASARILI", "$successCount kişiye SMS başarıyla ulaştı.")
            TaskNotificationManager.showMessageNotification(
                context,
                messageId.toInt(),
                "✅ Mesaj Başarıyla Ulaştı / İletildi",
                "${msg.recipientSummary} ($successCount kişi) mesajınız sorunsuz iletildi."
            )
        } else {
            val logStr = if (errorDetails.isNotEmpty()) errorDetails.joinToString("; ") else "Gönderilemedi"
            db.scheduledMessageDao().updateStatus(messageId, "BASARISIZ", logStr)
            TaskNotificationManager.showMessageNotification(
                context,
                messageId.toInt(),
                "❌ Mesaj Gönderilemedi!",
                "${msg.recipientSummary} kişisine mesaj iletilemedi:\n$logStr"
            )
        }
    }

    private suspend fun handleReminder(context: Context, db: AppDatabase, reminderId: Long) {
        val reminder = db.reminderDao().getReminderById(reminderId) ?: return
        TaskNotificationManager.showReminderNotification(
            context,
            reminderId.toInt(),
            reminder.title,
            if (reminder.description.isNotBlank()) reminder.description else reminder.title
        )
        db.reminderDao().updateReminder(reminder.copy(status = "BASARILI"))
    }

    private suspend fun handleCallTask(context: Context, db: AppDatabase, callId: Long) {
        val callTask = db.callTaskDao().getCallTaskById(callId) ?: return
        TaskNotificationManager.showCallReminderNotification(
            context,
            callId.toInt(),
            callTask.personName,
            callTask.phone,
            callTask.note
        )
        db.callTaskDao().updateCallTask(callTask.copy(status = "BASARILI"))
    }
}
