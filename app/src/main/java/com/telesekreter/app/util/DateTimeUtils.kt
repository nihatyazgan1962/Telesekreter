package com.telesekreter.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    private val trLocale = Locale("tr")

    fun formatDateTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", trLocale)
        return sdf.format(Date(timeMillis))
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", trLocale)
        return sdf.format(Date(timeMillis))
    }

    fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", trLocale)
        return sdf.format(Date(timeMillis))
    }

    fun isToday(timeMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getTodayStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
