package dev.drizzle.weather.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormat {

    private val hourMinute = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val hour = DateTimeFormatter.ofPattern("HH", Locale.getDefault())

    fun time(value: LocalDateTime): String = value.format(hourMinute)

    fun hourLabel(value: LocalDateTime): String = value.format(hour)

    fun relativeUpdated(updatedAt: Instant, now: Instant = Instant.now()): String {
        val minutes = Duration.between(updatedAt, now).toMinutes()
        return when {
            minutes <= 0 -> "Updated just now"
            minutes == 1L -> "Updated 1 min ago"
            minutes < 60 -> "Updated $minutes min ago"
            else -> {
                val hours = minutes / 60
                if (hours == 1L) "Updated 1 hour ago" else "Updated $hours hours ago"
            }
        }
    }

    fun isStale(updatedAt: Instant, now: Instant = Instant.now()): Boolean =
        Duration.between(updatedAt, now).toMinutes() >= 60
}
