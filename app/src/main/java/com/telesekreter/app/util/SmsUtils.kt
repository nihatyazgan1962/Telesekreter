package com.telesekreter.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.os.Build

object SmsUtils {

    fun sendSmsDirect(context: Context? = null, phoneNumber: String, message: String): Pair<Boolean, String> {
        return try {
            val cleanPhone = phoneNumber.replace("[^0-9+]".toRegex(), "")
            if (cleanPhone.isBlank()) return Pair(false, "Geçersiz telefon numarası.")
            if (message.isBlank()) return Pair(false, "Mesaj içeriği boş.")

            var smsManager: SmsManager? = null

            // 1. Yöntem: Android 12+ (API 31+) Context üzerinden getSystemService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
                try {
                    smsManager = context.getSystemService(SmsManager::class.java)
                } catch (e: Exception) {
                    smsManager = null
                }
            }

            // 2. Yöntem: SubscriptionId üzerinden aktif SIM kart SmsManager'ı bulma (Oppo / ColorOS Çift SIM desteği)
            if (smsManager == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context != null) {
                try {
                    val subId = SmsManager.getDefaultSmsSubscriptionId()
                    if (subId >= 0) {
                        smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.getSystemService(SmsManager::class.java)?.createForSubscriptionId(subId)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsManager.getSmsManagerForSubscriptionId(subId)
                        }
                    }
                } catch (e: Exception) {
                    // Fallback
                }
            }

            // 3. Yöntem: Standart getDefault() fallback
            if (smsManager == null) {
                @Suppress("DEPRECATION")
                smsManager = SmsManager.getDefault()
            }

            if (smsManager == null) {
                return Pair(false, "Telefonda aktif SIM kart veya SMS servisi bulunamadı.")
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanPhone, null, message, null, null)
            }
            Pair(true, "SMS başarıyla gönderildi.")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Pair(false, "SMS Gönderme İzni eksik (SEND_SMS). Lütfen ayarlardan izin verin.")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "SMS Gönderilemedi: ${e.localizedMessage ?: e.message}")
        }
    }

    fun openSmsApp(context: Context, phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumber.replace("[^0-9+]".toRegex(), "")}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        try {
            var cleanPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "90" + cleanPhone.substring(1)
            } else if (!cleanPhone.startsWith("90")) {
                cleanPhone = "90$cleanPhone"
            }
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
