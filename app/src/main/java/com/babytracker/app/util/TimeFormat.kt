package com.babytracker.app.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

fun formatFull(millis: Long): String = fullFormat.format(Date(millis))

/** 计算月龄描述：x 岁 y 个月 */
fun ageLabel(birthdayMillis: Long, now: Long = System.currentTimeMillis()): String {
    if (birthdayMillis <= 0) return ""
    val birth: LocalDate = Instant.ofEpochMilli(birthdayMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today: LocalDate = Instant.ofEpochMilli(now)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    if (birth.isAfter(today)) return ""
    val period = Period.between(birth, today)
    val months = period.years * 12 + period.months
    return when {
        period.years >= 1 -> "${period.years} 岁 ${period.months} 个月"
        else -> "${months} 个月"
    }
}
