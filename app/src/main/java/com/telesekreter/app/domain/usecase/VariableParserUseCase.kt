package com.telesekreter.app.domain.usecase

import com.telesekreter.app.data.local.entity.PersonEntity
import java.text.SimpleDateFormat
import java.util.*

object VariableParserUseCase {

    /**
     * Mesaj şablonundaki {AD}, {SOYAD}, {AD_SOYAD}, {TARIH}, {SAAT}, {GRUP}
     * etiketlerini kişi ve zaman bilgileriyle değiştirir.
     */
    fun parse(template: String, person: PersonEntity?, groupName: String? = null, dateMillis: Long = System.currentTimeMillis()): String {
        var result = template
        val name = person?.name ?: ""
        val surname = person?.surname ?: ""
        val fullName = person?.fullName ?: ""

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))
        val timeFormat = SimpleDateFormat("HH:mm", Locale("tr"))
        val date = Date(dateMillis)

        result = result.replace("{AD}", name, ignoreCase = true)
        result = result.replace("{SOYAD}", surname, ignoreCase = true)
        result = result.replace("{AD_SOYAD}", fullName, ignoreCase = true)
        result = result.replace("{TARIH}", dateFormat.format(date), ignoreCase = true)
        result = result.replace("{SAAT}", timeFormat.format(date), ignoreCase = true)
        result = result.replace("{GRUP}", groupName ?: "", ignoreCase = true)

        return result
    }
}
