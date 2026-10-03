# TELESEKRETER – Proje Mimarisi ve Teknik Tasarım

**Slogan:** "Zamanında Hatırla, Zamanında İlet."  
**Dil:** %100 Türkçe UI & Temiz Kotlin Kodu  
**Platform:** Android (Modern Jetpack Kütüphaneleri & Room DB & WorkManager & AlarmManager)

---

## 1. Mimari Yapı (Clean Architecture + MVVM)

```
app/src/main/java/com/telesekreter/app/
 ├── data/
 │    ├── local/
 │    │    ├── AppDatabase.kt
 │    │    ├── dao/
 │    │    │    ├── PersonDao.kt
 │    │    │    ├── GroupDao.kt
 │    │    │    ├── ScheduledMessageDao.kt
 │    │    │    ├── ReminderDao.kt
 │    │    │    ├── CallTaskDao.kt
 │    │    │    ├── MessageTemplateDao.kt
 │    │    │    └── SpecialDayDao.kt
 │    │    └── entity/
 │    │         ├── PersonEntity.kt
 │    │         ├── GroupEntity.kt
 │    │         ├── PersonGroupCrossRef.kt
 │    │         ├── ScheduledMessageEntity.kt
 │    │         ├── ReminderEntity.kt
 │    │         ├── CallTaskEntity.kt
 │    │         ├── MessageTemplateEntity.kt
 │    │         └── SpecialDayEntity.kt
 │    └── repository/
 │         ├── PersonRepository.kt
 │         ├── MessageRepository.kt
 │         ├── ReminderRepository.kt
 │         ├── CallTaskRepository.kt
 │         ├── TemplateRepository.kt
 │         └── BackupRepository.kt
 ├── domain/
 │    ├── model/
 │    │    ├── TaskStatus.kt
 │    │    ├── RepeatRule.kt
 │    │    ├── ChannelType.kt
 │    │    └── SpecialDayType.kt
 │    └── usecase/
 │         ├── VariableParserUseCase.kt
 │         ├── SendMessageUseCase.kt
 │         └── ScheduleTaskUseCase.kt
 ├── scheduler/
 │    ├── TaskAlarmReceiver.kt
 │    ├── TaskNotificationManager.kt
 │    ├── BootReceiver.kt
 │    └── MessageWorker.kt
 ├── ui/
 │    ├── main/ (Ana Sayfa - Büyük Butonlar & Günlük Özet)
 │    ├── contacts/ (Kişiler ve Gruplar)
 │    ├── messages/ (Zamanlanmış ve Toplu Mesaj Planlama)
 │    ├── reminders/ (Hatırlatıcılar)
 │    ├── birthdays/ (Doğum Günleri & Özel Günler)
 │    ├── calltasks/ (Arama Planlayıcı)
 │    ├── templates/ (Mesaj Şablonları & Değişkenler)
 │    ├── history/ (Görev Geçmişi & İstatistikler)
 │    └── settings/ (Yedekleme & Onay Tercihleri)
 └── util/
      ├── DateTimeUtils.kt
      ├── PermissionUtils.kt
      └── SmsUtils.kt
```

---

## 2. Kullanılan Teknolojiler

- **Kotlin 2.0+**: Modern, null-safe dil standardı
- **Jetpack Room**: SQLite tabanlı yüksek performanslı yerel veritabanı (İlişkisel tablolar & TypeConverters)
- **Android AlarmManager & BroadcastReceiver**: Hassas zamanlı alarmlar ve bildirimler
- **WorkManager**: Arka plan görevleri, tekrarlı kontroller ve yedekleme
- **Coroutines & Kotlin Flow**: Reaktif UI veri akışı ve asenkron işlemler
- **Material Design 3**: Yaşlı ve her yaştan kullanıcıya hitap eden geniş dokunma alanları, net kontrast ve büyük yazılar.
- **Android Notification Channels**: Özelleştirilmiş bildirim kategorileri (Mesaj, Arama, Hatırlatıcı, Doğum Günü)
