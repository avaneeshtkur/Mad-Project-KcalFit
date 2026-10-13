package com.kcalfit.app.domain.home

import java.time.ZonedDateTime
import java.time.Duration

fun greetingForHour(hour: Int): String {
    require(hour in 0..23)
    return when(hour) {
        in 5..11 -> "Good morning"
        in 12..15 -> "Good afternoon"
        in 16..20 -> "Good evening"
        else -> "Good night"
    }
}
fun millisUntilGreetingBoundary(now: ZonedDateTime): Long {
    val candidates = listOf(5,12,16,21).map { hour ->
        now.toLocalDate().atTime(hour,0).atZone(now.zone).let { if(it > now) it else it.plusDays(1) }
    }
    return Duration.between(now,candidates.minOrNull()).toMillis().coerceAtLeast(1)
}
fun calorieRingProgress(consumed: Int, goal: Int): Float =
    if (goal <= 0) 0f else (consumed.toFloat() / goal).coerceIn(0f,1f)
