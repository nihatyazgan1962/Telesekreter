package com.telesekreter.app.util

import android.content.Context
import android.provider.ContactsContract
import com.telesekreter.app.data.local.entity.PersonEntity

object ContactUtils {

    fun normalizePhoneNumber(phone: String): String {
        var clean = phone.replace("[^0-9+]".toRegex(), "")
        if (clean.startsWith("+90")) {
            clean = "0" + clean.substring(3)
        } else if (clean.startsWith("90") && clean.length == 12) {
            clean = "0" + clean.substring(2)
        }
        return clean
    }

    fun importPhoneContacts(context: Context): List<PersonEntity> {
        val contactsList = mutableListOf<PersonEntity>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            val seenNumbers = mutableSetOf<String>()

            while (it.moveToNext()) {
                val displayName = it.getString(nameIndex)?.trim() ?: ""
                val rawNumber = it.getString(numberIndex) ?: ""
                val normalizedNumber = normalizePhoneNumber(rawNumber)

                if (normalizedNumber.isNotBlank() && !seenNumbers.contains(normalizedNumber)) {
                    seenNumbers.add(normalizedNumber)
                    val parts = displayName.split(" ").filter { it.isNotBlank() }
                    val firstName = if (parts.isNotEmpty()) parts.first() else displayName
                    val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

                    contactsList.add(
                        PersonEntity(
                            name = firstName,
                            surname = lastName,
                            phone = normalizedNumber,
                            notes = "Rehberden Aktarıldı"
                        )
                    )
                }
            }
        }
        return contactsList
    }
}
