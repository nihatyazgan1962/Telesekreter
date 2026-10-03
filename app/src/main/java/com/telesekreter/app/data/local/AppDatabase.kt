package com.telesekreter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.telesekreter.app.data.local.dao.*
import com.telesekreter.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonEntity::class,
        GroupEntity::class,
        PersonGroupCrossRef::class,
        ScheduledMessageEntity::class,
        ReminderEntity::class,
        CallTaskEntity::class,
        MessageTemplateEntity::class,
        SpecialDayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun reminderDao(): ReminderDao
    abstract fun callTaskDao(): CallTaskDao
    abstract fun messageTemplateDao(): MessageTemplateDao
    abstract fun specialDayDao(): SpecialDayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "telesekreter_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialTemplates(database.messageTemplateDao())
                        populateInitialGroups(database.groupDao())
                    }
                }
            }

            suspend fun populateInitialGroups(groupDao: GroupDao) {
                val groups = listOf(
                    GroupEntity(name = "Aile", description = "Aile fertleri"),
                    GroupEntity(name = "Arkadaşlar", description = "Yakın dostlar ve arkadaşlar"),
                    GroupEntity(name = "İş", description = "İş arkadaşları ve ortaklar"),
                    GroupEntity(name = "Dernek / Topluluk", description = "Dernek ve vakıf üyeleri"),
                    GroupEntity(name = "Komşular", description = "Mahalle ve bina komşuları")
                )
                groups.forEach { groupDao.insertGroup(it) }
            }

            suspend fun populateInitialTemplates(templateDao: MessageTemplateDao) {
                val templates = listOf(
                    MessageTemplateEntity(
                        title = "Hayırlı Sabahlar",
                        category = "Günaydın",
                        content = "Günaydın {AD}, hayırlı ve bereketli bir gün geçirmeni dilerim.",
                        isDefault = true
                    ),
                    MessageTemplateEntity(
                        title = "Huzurlu Akşamlar",
                        category = "İyi Akşamlar",
                        content = "İyi akşamlar {AD}, ailenle birlikte huzur dolu bir akşam dilerim.",
                        isDefault = true
                    ),
                    MessageTemplateEntity(
                        title = "Doğum Günü Kutlaması",
                        category = "Doğum Günü",
                        content = "Sevgili {AD}, yeni yaşın sağlık, mutluluk ve huzur getirsin. Doğum günün kutlu olsun!",
                        isDefault = true
                    ),
                    MessageTemplateEntity(
                        title = "Bayram Tebriği",
                        category = "Bayram",
                        content = "Bayramınız mübarek olsun. Aileniz ve sevdiklerinizle birlikte nice mutlu ve huzurlu bayramlara.",
                        isDefault = true
                    ),
                    MessageTemplateEntity(
                        title = "Cuma Tebriği",
                        category = "Özel",
                        content = "Hayırlı Cumalar {AD}. Dualarınızın kabul olması dileğiyle.",
                        isDefault = true
                    ),
                    MessageTemplateEntity(
                        title = "Genel Hatırlatma",
                        category = "Hatırlatma",
                        content = "Merhaba {AD}, bugün yapmamız gereken işi hatırlatmak istedim.",
                        isDefault = true
                    )
                )
                templateDao.insertAll(templates)
            }
        }
    }
}
