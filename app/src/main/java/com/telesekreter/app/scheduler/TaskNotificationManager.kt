package com.telesekreter.app.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.telesekreter.app.ui.MainActivity

object TaskNotificationManager {

    const val CHANNEL_MESSAGES = "telesekreter_messages"
    const val CHANNEL_CALLS = "telesekreter_calls"
    const val CHANNEL_REMINDERS = "telesekreter_reminders"
    const val CHANNEL_SPECIAL_DAYS = "telesekreter_special_days"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Zamanlanmış Mesajlar",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Zamanlanmış ve onay bekleyen mesaj bildirimleri"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_CALLS,
                    "Zamanlanmış Aramalar",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Zamanlanmış telefon görüşmesi hatırlatmaları"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Hatırlatıcılar",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Günlük ve genel hatırlatıcı bildirimleri"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_SPECIAL_DAYS,
                    "Doğum Günleri ve Özel Günler",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Doğum günleri ve özel gün uyarıları"
                    enableVibration(true)
                }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun showMessageNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        isPendingApproval: Boolean = false,
        messageId: Long? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("OPEN_TAB", "MESSAGES")
            if (isPendingApproval) {
                putExtra("FILTER", "ONAY_BEKLIYOR")
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Eğer onay bekleyen mesajsa doğrudan bildirim üzerinden ONAYLA ve İPTAL ET butonları ekle
        if (isPendingApproval && messageId != null) {
            // Doğrudan Onayla & Gönder Broadcast Intent
            val approveIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = TaskAlarmReceiver.ACTION_APPROVE_SEND
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, messageId)
                putExtra(TaskAlarmScheduler.EXTRA_TASK_TYPE, TaskAlarmScheduler.TYPE_MESSAGE)
            }
            val approvePendingIntent = PendingIntent.getBroadcast(
                context,
                (messageId * 100 + 1).toInt(),
                approveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // İptal Et Broadcast Intent
            val cancelIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = TaskAlarmReceiver.ACTION_CANCEL_TASK
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, messageId)
                putExtra(TaskAlarmScheduler.EXTRA_TASK_TYPE, TaskAlarmScheduler.TYPE_MESSAGE)
            }
            val cancelPendingIntent = PendingIntent.getBroadcast(
                context,
                (messageId * 100 + 2).toInt(),
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(android.R.drawable.ic_menu_send, "🚀 ONAYLA & GÖNDER", approvePendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "❌ İPTAL ET", cancelPendingIntent)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    fun showCallReminderNotification(
        context: Context,
        notificationId: Int,
        personName: String,
        phoneNumber: String,
        note: String
    ) {
        // Doğrudan Arama Butonu Intent'i
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val callPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1000,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = if (note.isNotBlank()) "Not: $note" else "$phoneNumber aranacak."

        val builder = NotificationCompat.Builder(context, CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Arama Zamanı: $personName")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_menu_call, "ŞİMDİ ARA", callPendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    fun showReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        description: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Hatırlatıcı: $title")
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}
