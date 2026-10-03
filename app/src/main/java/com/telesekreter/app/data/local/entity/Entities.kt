package com.telesekreter.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val surname: String = "",
    val phone: String,
    val birthDate: String? = null, // YYYY-MM-DD
    val notes: String = "",
    val isImportant: Boolean = false
) {
    val fullName: String
        get() = if (surname.isBlank()) name else "$name $surname"
}

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(
    tableName = "person_group_cross_ref",
    primaryKeys = ["personId", "groupId"]
)
data class PersonGroupCrossRef(
    val personId: Long,
    val groupId: Long
)

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientPersonIds: String, // Comma separated IDs (örn: "1,2,5")
    val groupId: Long? = null,
    val recipientSummary: String, // Gösterim için isimler özeti
    val messageText: String,
    val scheduledTimeMillis: Long,
    val status: String = "PLANLANDI",
    val repeatRule: String = "ONCE",
    val channel: String = "SMS",
    val requireConfirmation: Boolean = true,
    val logMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scheduledTimeMillis: Long,
    val repeatRule: String = "ONCE",
    val status: String = "PLANLANDI",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_tasks")
data class CallTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val personName: String,
    val phone: String,
    val note: String = "",
    val scheduledTimeMillis: Long,
    val status: String = "PLANLANDI",
    val repeatRule: String = "ONCE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Günaydın", "İyi Akşamlar", "Doğum Günü", "Bayram", "Hatırlatma", "Özel"
    val content: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "special_days")
data class SpecialDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val personId: Long? = null,
    val personName: String? = null,
    val dayOfMonth: Int,
    val month: Int, // 1-12
    val year: Int? = null, // Opsiyonel (varsa yıldönümü hesabı için)
    val type: String = "OZEL",
    val autoMessageEnabled: Boolean = false,
    val templateId: Long? = null
)
