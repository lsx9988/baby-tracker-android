package com.babytracker.app.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

fun formatFull(millis: Long): String = fullFormat.format(Date(millis))

fun formatDateOnly(millis: Long): String = dateOnlyFormat.format(Date(millis))

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

/** 当天 0 点的时间戳 */
fun startOfDay(millis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun isSameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

/** 时间线分组标签：今天 / 昨天 / MM-dd */
fun dayLabel(millis: Long, now: Long = System.currentTimeMillis()): String {
    if (isSameDay(millis, now)) return "今天"
    val yesterday = now - 24L * 60 * 60_000
    if (isSameDay(millis, yesterday)) return "昨天"
    return formatDate(millis)
}

/** 相对时长描述：距上次喂奶等 */
fun relativeAgo(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - millis
    if (diff < 0) return formatTime(millis)
    val min = diff / 60_000
    if (min < 1) return "刚刚"
    if (min < 60) return "${min} 分钟前"
    val h = min / 60
    if (h < 24) return "${h} 小时 ${min % 60} 分前"
    val d = h / 24
    return "${d} 天前"
}
