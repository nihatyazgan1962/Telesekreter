package com.telesekreter.app.data.local.dao

import androidx.room.*
import com.telesekreter.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY isImportant DESC, name ASC")
    fun getAllPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE isImportant = 1 ORDER BY name ASC")
    fun getImportantPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Long): PersonEntity?

    @Query("SELECT * FROM persons WHERE id IN (:ids)")
    suspend fun getPersonsByIds(ids: List<Long>): List<PersonEntity>

    @Query("SELECT * FROM persons ORDER BY isImportant DESC, name ASC")
    suspend fun getAllPersonsListSync(): List<PersonEntity>

    @Query("SELECT * FROM persons WHERE phone = :phone LIMIT 1")
    suspend fun getPersonByPhone(phone: String): PersonEntity?

    @Query("DELETE FROM persons")
    suspend fun deleteAllPersons()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    // Grup ve Kişi İlişkisi
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersonGroupCrossRef(crossRef: PersonGroupCrossRef)

    @Delete
    suspend fun deletePersonGroupCrossRef(crossRef: PersonGroupCrossRef)

    @Query("DELETE FROM person_group_cross_ref WHERE personId = :personId")
    suspend fun clearGroupsForPerson(personId: Long)

    @Query("""
        SELECT p.* FROM persons p
        INNER JOIN person_group_cross_ref ref ON p.id = ref.personId
        WHERE ref.groupId = :groupId
        ORDER BY p.name ASC
    """)
    fun getPersonsInGroup(groupId: Long): Flow<List<PersonEntity>>

    @Query("""
        SELECT p.* FROM persons p
        INNER JOIN person_group_cross_ref ref ON p.id = ref.personId
        WHERE ref.groupId = :groupId
    """)
    suspend fun getPersonsInGroupSync(groupId: Long): List<PersonEntity>
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)
}

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTimeMillis ASC")
    fun getAllMessages(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE status IN ('PLANLANDI', 'BEKLIYOR', 'ONAY_BEKLIYOR') ORDER BY scheduledTimeMillis ASC")
    fun getPendingMessages(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE scheduledTimeMillis BETWEEN :startTime AND :endTime ORDER BY scheduledTimeMillis ASC")
    fun getMessagesForDateRange(startTime: Long, endTime: Long): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): ScheduledMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ScheduledMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ScheduledMessageEntity)

    @Delete
    suspend fun deleteMessage(message: ScheduledMessageEntity)

    @Query("UPDATE scheduled_messages SET status = :status, logMessage = :log WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, log: String? = null)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledTimeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE status = 'PLANLANDI' ORDER BY scheduledTimeMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}

@Dao
interface CallTaskDao {
    @Query("SELECT * FROM call_tasks ORDER BY scheduledTimeMillis ASC")
    fun getAllCallTasks(): Flow<List<CallTaskEntity>>

    @Query("SELECT * FROM call_tasks WHERE status = 'PLANLANDI' ORDER BY scheduledTimeMillis ASC")
    fun getPendingCallTasks(): Flow<List<CallTaskEntity>>

    @Query("SELECT * FROM call_tasks WHERE id = :id")
    suspend fun getCallTaskById(id: Long): CallTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallTask(callTask: CallTaskEntity): Long

    @Update
    suspend fun updateCallTask(callTask: CallTaskEntity)

    @Delete
    suspend fun deleteCallTask(callTask: CallTaskEntity)
}

@Dao
interface MessageTemplateDao {
    @Query("SELECT * FROM message_templates ORDER BY category ASC, id ASC")
    fun getAllTemplates(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates WHERE category = :category")
    fun getTemplatesByCategory(category: String): Flow<List<MessageTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<MessageTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: MessageTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: MessageTemplateEntity)
}

@Dao
interface SpecialDayDao {
    @Query("SELECT * FROM special_days ORDER BY month ASC, dayOfMonth ASC")
    fun getAllSpecialDays(): Flow<List<SpecialDayEntity>>

    @Query("SELECT * FROM special_days WHERE month = :month AND dayOfMonth = :day")
    suspend fun getSpecialDaysForDate(month: Int, day: Int): List<SpecialDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialDay(specialDay: SpecialDayEntity): Long

    @Update
    suspend fun updateSpecialDay(specialDay: SpecialDayEntity)

    @Delete
    suspend fun deleteSpecialDay(specialDay: SpecialDayEntity)
}
