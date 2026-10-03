# TELESEKRETER – Akıllı Zamanlanmış İletişim ve Hatırlatma Uygulaması

## 1. Proje Amacı

TELESEKRETER; kullanıcının seçtiği kişi veya kişilere belirli tarih ve saatte mesaj göndermesini, toplu mesajlar hazırlamasını, hatırlatmalar oluşturmasını, doğum günlerini takip etmesini ve belirlenen zamanda seçilen kişiyi aramasını sağlayan Android öncelikli kişisel iletişim asistanıdır.

Uygulama sade, Türkçe, kolay kullanılabilir ve yaşça büyük kullanıcıların da rahat kullanabileceği şekilde tasarlanmalıdır.

Ana fikir:

> "Kullanıcı bir kez planlasın, TELESEKRETER zamanı geldiğinde hatırlatsın veya izin verilen işlemi gerçekleştirsin."

---

## 2. Ana Özellikler

### 2.1 Zamanlanmış Mesaj

Kullanıcı:
- Rehberden kişi seçebilmeli.
- Birden fazla kişi seçebilmeli.
- Mesaj yazabilmeli.
- Tarih ve saat seçebilmeli.
- Gönderimi planlayabilmeli.
- Planlanan mesajları görebilmeli.
- Mesajı düzenleyebilmeli.
- Mesajı iptal edebilmeli.

Örnek:

"Kemal'e 5 Ekim 2026 saat 09:00'da Günaydın, hayırlı işler mesajı gönder."

Mesaj durumları:
- Bekliyor
- Gönderildi
- Başarısız
- İptal edildi

---

## 3. Toplu Mesaj

Kullanıcı:
- Birden fazla kişi seçebilmeli.
- Grup seçebilmeli.
- Mesaj hazırlayabilmeli.
- Tarih/saat belirleyebilmeli.
- Toplu gönderimi planlayabilmeli.

Örnek gruplar:
- Aile
- Arkadaşlar
- Dernek
- İş
- Komşular
- Özel

Kişilerin telefon numaraları birbirlerine gösterilmemeli. Mümkün olduğunda mesajlar kişiye ayrı ayrı gönderilmelidir.

---

## 4. Kişi Grupları

Kullanıcı özel gruplar oluşturabilmeli.

Örnek:
- Ailem
- Çocuklar
- Kardeşler
- Dernek Yönetimi
- Arkadaşlar
- Önemli Kişiler

Bir kişiyi birden fazla gruba eklemek mümkün olmalıdır.

---

## 5. Doğum Günü Sistemi

Kişinin:
- Adı
- Telefonu
- Doğum tarihi
- Notu
- Grubu

kaydedilebilmeli.

Doğum günü yaklaşınca bildirim gösterilmeli.

Örnek:
"Yarın Ahmet'in doğum günü."

Kullanıcı hazır mesaj seçebilmeli:

"Doğum günün kutlu olsun. Sağlıklı, huzurlu ve mutlu nice yıllara."

Mesaj:
- Hemen gönderilebilir.
- Belirlenen saatte gönderilmek üzere planlanabilir.
- Kullanıcıdan gönderim öncesi onay istenebilir.

Varsayılan güvenli davranış:
Doğum günü mesajları otomatik gönderilmeden önce kullanıcıya bildirim/önizleme gösterilmesi.

---

## 6. Özel Günler

Kullanıcı özel günler oluşturabilmeli.

Örnek:
- Evlilik yıldönümü
- Tanışma yıldönümü
- Sünnet
- Bayram
- Kandil
- Anneler Günü
- Babalar Günü
- Yılbaşı
- Kişisel özel gün

Her özel gün için mesaj şablonu oluşturulabilmeli.

---

## 7. Hatırlatıcı Sistemi

Kullanıcı tarih ve saat belirleyerek hatırlatıcı oluşturabilmeli.

Örnek:
- Saat 14:00'te Ahmet'i ara.
- 5 Ekim'de aidatı öde.
- Her ayın 1'inde fatura kontrolü yap.
- Her cuma saat 10:00'da toplantıyı hatırlat.
- 15 dakika sonra ilaç/iş hatırlatması.

Tekrarlama seçenekleri:
- Bir kez
- Her gün
- Her hafta
- Her ay
- Her yıl
- Özel tekrar

---

## 8. Zamanlanmış Telefon Araması

Kullanıcı:
- Rehberden kişi seçebilmeli.
- Tarih ve saat seçebilmeli.
- Arama hatırlatıcısı oluşturabilmeli.

Örnek:

"5 Ekim saat 18:30'da Mehmet'i ara."

ÖNEMLİ:
Android sürümüne ve cihaz üreticisine göre uygulamanın kullanıcı onayı olmadan doğrudan arama başlatmasına izin verilmeyebilir.

Bu nedenle iki mod tasarlanmalıdır:

1. Güvenli mod:
Zamanı gelince bildirim gösterilir ve kullanıcı "Ara" butonuna basar.

2. Cihaz/Android izinleri destekliyorsa:
Uygulama izin verilen ölçüde doğrudan arama ekranını açabilir.

Uygulama Android kısıtlamalarını aşmaya çalışmamalıdır.

---

## 9. Arama Geçmişi

Uygulama mümkün olduğu ölçüde:
- Planlanan aramaları
- Tamamlanan görevleri
- İptal edilen aramaları

gösterebilmelidir.

Telefonun gerçek arama geçmişine erişim gerekiyorsa Android izinleri ve Google Play politikaları dikkate alınmalıdır.

---

## 10. Mesaj Şablonları

Kullanıcı sık kullandığı mesajları kaydedebilmeli.

Örnek kategoriler:

### Günaydın
"Günaydın, hayırlı ve güzel bir gün geçirmeni dilerim."

### İyi Akşamlar
"İyi akşamlar, Allah huzur ve sağlık versin."

### Doğum Günü
"Doğum günün kutlu olsun. Sağlık, huzur ve mutluluk içinde nice yıllara."

### Bayram
"Bayramınız mübarek olsun. Ailenizle birlikte sağlık, huzur ve mutluluk dolu nice bayramlara."

### Hatırlatma
"Merhaba, bugün yapmamız gereken işi hatırlatmak istedim."

Şablonlar:
- Eklenebilmeli
- Düzenlenebilmeli
- Silinebilmeli
- Kategorilere ayrılabilmeli

---

## 11. Değişkenli Mesajlar

Mesaj şablonlarında kişi adı kullanılabilmeli.

Örnek:

"Merhaba {AD}, doğum günün kutlu olsun."

Sistem gönderim sırasında:

"Merhaba Ahmet, doğum günün kutlu olsun."

şeklinde oluşturmalıdır.

Desteklenebilecek değişkenler:
- {AD}
- {SOYAD}
- {AD_SOYAD}
- {TARIH}
- {SAAT}
- {GRUP}

---

## 12. Ana Sayfa

Ana ekran çok sade olmalıdır.

Büyük butonlar:

+ MESAJ GÖNDER
+ ZAMANLA
+ TOPLU MESAJ
+ KİŞİLER
+ DOĞUM GÜNLERİ
+ HATIRLATICILAR
+ ARAMA PLANLA
+ MESAJ ŞABLONLARI
+ GEÇMİŞ

Ana ekranda ayrıca:

Bugünkü görevler
Yaklaşan doğum günleri
Bekleyen mesajlar
Yaklaşan aramalar

gösterilmelidir.

---

## 13. Takvim Görünümü

Aylık takvim bulunmalıdır.

Takvim üzerinde:
- Mesajlar
- Aramalar
- Hatırlatıcılar
- Doğum günleri
- Özel günler

işaretlenmelidir.

Bir güne tıklanınca o güne ait görevler gösterilmelidir.

---

## 14. Bildirim Sistemi

Bildirimler:
- Yaklaşan mesaj
- Mesaj gönderildi
- Mesaj gönderilemedi
- Arama zamanı geldi
- Hatırlatıcı zamanı geldi
- Doğum günü
- Özel gün

için kullanılmalıdır.

Kullanıcı bildirimleri ayrı ayrı açıp kapatabilmelidir.

---

## 15. Gönderim Öncesi Onay

Güvenlik amacıyla kullanıcı ayarlardan:

"Zamanı gelen mesajları otomatik gönder"

veya

"Mesaj gönderilmeden önce bana sor"

seçeneklerinden birini seçebilmelidir.

Varsayılan ayar güvenli seçenek olmalıdır:
"Önce bana sor."

---

## 16. Sesli Komut Sistemi

İleri aşamada sesli komut desteklenmelidir.

Örnek:

"Yarın saat 10'da Ahmet'i ara."

"Cumartesi saat 09'da aile grubuna günaydın mesajı gönder."

"Yarın Mehmet'in doğum günü."

"Bugün hangi görevlerim var?"

Sesli komut önce metne çevrilmeli, sonra kullanıcıya planın özeti gösterilmeli ve gerekirse onay alınmalıdır.

---

## 17. Akıllı Asistan

İleri sürümde TELESEKRETER doğal Türkçe komutları anlayabilmelidir.

Örnek:

"Yarın sabah 8'de çocuklara günaydın mesajı gönder."

Sistem:
- Tarihi belirler.
- Saati belirler.
- Grubu bulur.
- Mesajı oluşturur.
- Kullanıcıya onay ekranı gösterir.

Kullanıcı:
"Onayla."

dediğinde işlem planlanır.

---

## 18. WhatsApp / Telegram / E-posta

Mimari baştan çoklu iletişim kanallarına uygun hazırlanmalıdır.

İleride desteklenebilecek kanallar:
- SMS
- Telefon
- WhatsApp
- Telegram
- E-posta
- Bildirim

ÖNEMLİ:
WhatsApp gibi üçüncü taraf uygulamalarda otomatik mesaj gönderme imkanları resmi API ve platform kurallarına bağlıdır. Uygulama bu kısıtlamaları aşmaya çalışmamalıdır.

---

## 19. Yedekleme

Kullanıcının oluşturduğu:
- Kişi grupları
- Doğum günleri
- Hatırlatıcılar
- Mesaj şablonları
- Planlanmış görevler

yedeklenebilmelidir.

İlk sürüm:
- Yerel yedekleme
- JSON/ZIP dışa aktarma
- İçeri aktarma

İleri sürüm:
- Google Drive veya başka güvenli bulut yedekleme.

---

## 20. Arama ve Filtreleme

Kullanıcı:
- Kişi adına göre
- Gruba göre
- Tarihe göre
- Görev türüne göre
- Duruma göre

arama yapabilmelidir.

---

## 21. Güvenlik ve Gizlilik

Kişisel veriler korunmalıdır.

Özellikle:
- Telefon numaraları
- Doğum tarihleri
- Mesaj içerikleri
- Görevler

güvenli şekilde saklanmalıdır.

Uygulama gereksiz izin istememelidir.

Gerekli izinler açık ve anlaşılır şekilde açıklanmalıdır.

---

## 22. Android İzinleri

Uygulama geliştirilirken Android sürümüne göre izinler kontrol edilmelidir.

Olası izinler:
- Kişilere erişim
- Telefon arama
- SMS gönderme
- Bildirim
- Alarm/zamanlanmış görev
- İsteğe bağlı takvim erişimi

Her izin gerçekten ihtiyaç olduğunda istenmelidir.

Google Play Store politika ve Android güvenlik kuralları dikkate alınmalıdır.

---

## 23. Zamanlama Motoru

Zamanlama sistemi uygulama kapalıyken de çalışabilecek şekilde tasarlanmalıdır.

Android'in modern sürümlerindeki:
- AlarmManager
- WorkManager
- Notification sistemi

uygun kullanım senaryolarına göre değerlendirilmelidir.

Tam zamanında çalışması gereken görevler için Android'in alarm ve pil tasarrufu kısıtlamaları dikkate alınmalıdır.

---

## 24. Görev Durumları

Her planlanan işlem aşağıdaki durumlardan birine sahip olmalıdır:

- PLANLANDI
- BEKLİYOR
- ONAY BEKLİYOR
- ÇALIŞIYOR
- BAŞARILI
- BAŞARISIZ
- İPTAL EDİLDİ

Hata durumunda kullanıcıya anlaşılır hata mesajı gösterilmelidir.

---

## 25. Tekrarlayan Görevler

Desteklenmeli:

Her gün:
"Her gün saat 08:00"

Her hafta:
"Her cuma saat 10:00"

Her ay:
"Her ayın 1'i saat 09:00"

Her yıl:
"Her yıl 5 Ekim"

Özel:
"Her 15 günde bir"

---

## 26. Akıllı Öneriler

İleri sürümde uygulama kullanıcıya öneriler gösterebilir:

"Yarın 3 kişinin doğum günü var."

"Bugün 2 planlanmış aramanız var."

"Bu hafta 5 hatırlatıcınız bulunuyor."

"Geçen ay kullandığınız 3 mesaj şablonu var."

Ancak kullanıcı onayı olmadan önemli bir işlem yapılmamalıdır.

---

## 27. Acil Durum / Önemli Kişiler

Kullanıcı "Önemli Kişiler" listesi oluşturabilmelidir.

Örneğin:
- Aile
- Doktor
- İş
- Dernek
- Acil durum kişileri

Bu kişiler ana ekranda hızlı erişim sağlayacak şekilde gösterilebilir.

Bu özellik acil servis yerine geçmemelidir.

---

## 28. Hızlı İşlemler

Ana ekranda:

"Şimdi Mesaj Gönder"

"Şimdi Ara"

"10 Dakika Sonra Hatırlat"

"Yarın Hatırlat"

"Bu Akşam Hatırlat"

gibi hızlı seçenekler bulunabilir.

---

## 29. İstatistikler

İsteğe bağlı olarak:

- Gönderilen mesaj sayısı
- Başarısız mesaj sayısı
- Tamamlanan görevler
- İptal edilen görevler
- En çok kullanılan mesaj şablonları

gösterilebilir.

Kişisel verileri gereksiz şekilde sunmamalıdır.

---

## 30. Tasarım

Tasarım:
- Türkçe
- Sade
- Büyük yazılar
- Büyük butonlar
- Kolay okunabilir
- Yaşlı kullanıcı dostu
- Açık/koyu tema
- Telefon ekranlarına uyumlu

olmalıdır.

Ana renkler ve tema daha sonra ayarlardan değiştirilebilir.

---

# 31. Teknik Mimari

Antigravity projeyi modüler geliştirmelidir.

Önerilen katmanlar:

UI
↓
ViewModel / State
↓
Use Cases
↓
Repository
↓
Local Database
↓
Android Services / Scheduler

Veri modeli örnekleri:

Person
- id
- name
- surname
- phone
- birthDate
- notes
- groups

Group
- id
- name

ScheduledMessage
- id
- personIds
- groupId
- message
- scheduledDateTime
- status
- repeatRule
- channel

Reminder
- id
- title
- description
- dateTime
- repeatRule
- status

CallTask
- id
- personId
- dateTime
- status

MessageTemplate
- id
- title
- category
- content

---

# 32. Veritabanı

İlk Android sürümünde yerel veritabanı kullanılmalıdır.

Tercih:
Room Database veya Android platformuna uygun modern eşdeğeri.

Veritabanı ilişkileri temiz tasarlanmalıdır.

Kişiler ile gruplar arasında çoktan çoğa ilişki desteklenmelidir.

---

# 33. MVP – İlk Sürüm

İlk sürüm gereksiz şekilde büyütülmemelidir.

Öncelikli özellikler:

1. Kişiler
2. Gruplar
3. Mesaj yazma
4. Zamanlanmış mesaj
5. Toplu mesaj
6. Hatırlatıcı
7. Doğum günü
8. Zamanlanmış arama hatırlatıcısı
9. Bildirim
10. Mesaj şablonları
11. Görev geçmişi
12. Ayarlar
13. Yedekleme

Bu özellikler stabil çalıştıktan sonra sesli komut ve akıllı asistan eklenmelidir.

---

# 34. Kullanıcı Deneyimi

Yeni görev oluşturma süreci mümkün olduğunca kısa olmalıdır.

Örnek:

1. + butonuna bas
2. "Mesaj" seç
3. Kişi seç
4. Mesaj yaz
5. Tarih seç
6. Saat seç
7. "Planla" butonuna bas

Son ekranda:

"Kemal'e 5 Ekim 2026 saat 09:00'da mesaj gönderilecek."

[İPTAL] [PLANLA]

şeklinde onay gösterilmelidir.

---

# 35. Hata Yönetimi

Uygulama:
- İnternet yok
- SIM kart yok
- SMS gönderilemedi
- Kişi bulunamadı
- Telefon numarası geçersiz
- İzin verilmedi
- Bildirim izni kapalı
- Alarm kısıtlaması
- Pil optimizasyonu

gibi durumları anlaşılır Türkçe mesajlarla kullanıcıya bildirmelidir.

---

# 36. Antigravity'den Beklenen Geliştirme Süreci

Projeyi tek seferde karmaşık hale getirme.

Aşağıdaki sırayla geliştir:

FAZ 1:
- Proje kurulumu
- Ana ekran
- Navigasyon
- Tema
- Veritabanı

FAZ 2:
- Kişiler
- Gruplar
- Kişi seçme

FAZ 3:
- Mesaj oluşturma
- Zamanlama
- Bildirim
- Görev geçmişi

FAZ 4:
- Toplu mesaj
- Mesaj şablonları
- Değişkenli mesajlar

FAZ 5:
- Doğum günleri
- Özel günler
- Hatırlatıcılar

FAZ 6:
- Arama planlama
- Telefon entegrasyonu

FAZ 7:
- Yedekleme
- Geri yükleme
- Güvenlik

FAZ 8:
- Sesli komut
- Akıllı doğal dil asistanı

FAZ 9:
- İleri entegrasyonlar

Her faz tamamlandıktan sonra uygulama derlenmeli, test edilmeli ve hatalar düzeltilmelidir.

---

# 37. Önemli Güvenlik Kuralı

TELESEKRETER kullanıcı adına iletişim işlemleri yapabildiği için yanlış kişiye veya yanlış zamanda mesaj gönderilmesini önlemek önemlidir.

Özellikle ilk sürümde:
- Alıcıyı açıkça göster.
- Tarih ve saati açıkça göster.
- Mesajın tamamını göster.
- Toplu gönderimde kaç kişinin seçildiğini göster.
- Kritik işlemlerde onay iste.
- İptal seçeneği sun.

---

# 38. Gelecekte Eklenebilecek Özellikler

Aşağıdaki özellikler ileri sürüm için değerlendirilebilir:

- Yapay zekâ destekli mesaj yazma
- Mesajı daha resmi/duygusal/kısa hale getirme
- Sesli mesaj hazırlama
- Takvim entegrasyonu
- E-posta zamanlama
- Telegram entegrasyonu
- WhatsApp resmi API entegrasyonu
- Google Drive yedekleme
- Çoklu cihaz senkronizasyonu
- Kişisel görev listesi
- Aile takvimi
- Dernek/iş grubu iletişim yönetimi
- Şablon kategorileri
- Akıllı tekrar önerileri
- Konuşarak görev oluşturma
- "Bugün ne yapmam gerekiyor?" asistanı
- Ana ekranda günlük özet
- Önemli kişi kısayolları

---

# 39. Projenin Temel İlkesi

TELESEKRETER bir "otomatik mesaj gönderme uygulaması" olarak değil;

> "Kullanıcının telefon görüşmelerini, mesajlarını, doğum günlerini, özel günlerini ve günlük hatırlatmalarını zamanında yönetmesine yardımcı olan kişisel iletişim asistanı"

olarak tasarlanmalıdır.

Uygulama kullanıcı kontrolünü ön planda tutmalı, açık izinler kullanmalı ve Android/Google Play güvenlik kurallarına uygun geliştirilmelidir.

---

# 40. Antigravity İçin İlk Görev

Bu dokümanı proje gereksinimi olarak kabul et.

Önce:
1. Proje mimarisini oluştur.
2. Kullanılacak teknoloji ve kütüphaneleri belirle.
3. Veritabanı şemasını oluştur.
4. Ana ekranı oluştur.
5. Navigasyon sistemini oluştur.
6. Kişiler ve gruplar modülünü geliştir.
7. İlk çalışan MVP'yi oluştur.
8. Her aşamada uygulamayı test et.
9. Hataları düzelt.
10. Bir sonraki faza geçmeden önce mevcut fazın stabil olduğundan emin ol.

Kod üretirken temiz, modüler, sürdürülebilir ve ileride yeni iletişim kanalları eklenmesine uygun mimari kullan.

Tüm kullanıcı arayüzü metinleri Türkçe olmalıdır.

Uygulama adı:

TELESEKRETER

Alt başlık:

"Zamanında Hatırla, Zamanında İlet."

