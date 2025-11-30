package com.apk.koshub.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeAgo {
    // expecting created_at format "yyyy-MM-dd HH:mm:ss"
    fun getTimeAgo(dateString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = format.parse(dateString)
            val now = Date()
            val diff = now.time - (date?.time ?: 0)

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                minutes < 1 -> "Baru saja"
                minutes < 60 -> "$minutes menit lalu"
                hours < 24 -> "$hours jam lalu"
                days < 7 -> "$days hari lalu"
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date ?: now)
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
