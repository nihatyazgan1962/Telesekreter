package com.telesekreter.app.domain.model

enum class TaskStatus(val displayName: String) {
    PLANLANDI("Planlandı"),
    BEKLIYOR("Bekliyor"),
    ONAY_BEKLIYOR("Onay Bekliyor"),
    CALISIYOR("Çalışıyor"),
    BASARILI("Başarılı"),
    BASARISIZ("Başarısız"),
    IPTAL_EDILDI("İptal Edildi")
}

enum class RepeatRule(val displayName: String) {
    ONCE("Bir Kez"),
    DAILY("Her Gün"),
    WEEKLY("Her Hafta"),
    MONTHLY("Her Ay"),
    YEARLY("Her Yıl"),
    CUSTOM("Özel Tekrar")
}

enum class ChannelType(val displayName: String) {
    SMS("SMS"),
    PHONE_CALL("Telefon Araması"),
    WHATSAPP("WhatsApp"),
    NOTIFICATION("Sadece Bildirim")
}

enum class SpecialDayType(val displayName: String) {
    DOGUM_GUNU("Doğum Günü"),
    EVLILIK_YILDONUMU("Evlilik Yıldönümü"),
    TANISMA_YILDONUMU("Tanışma Yıldönümü"),
    BAYRAM("Bayram"),
    KANDIL("Kandil"),
    ANNELER_GUNU("Anneler Günü"),
    BABALAR_GUNU("Babalar Günü"),
    YILBASI("Yılbaşı"),
    OZEL("Özel Gün")
}
