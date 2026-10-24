package com.kcalfit.app.domain.auth

/** Device-wide UI throttle; server-side Supabase Auth limits remain authoritative. */
class LoginThrottle(
    var failures: Int = 0,
    var blockedUntil: Long = 0,
    private val now: () -> Long = System::currentTimeMillis
) {
    fun remainingSeconds(): Int {
        if (blockedUntil > 0 && now() >= blockedUntil) reset()
        return ((blockedUntil - now()).coerceAtLeast(0).coerceAtMost(60_000) + 999).div(1000).toInt()
    }
    fun failed(): Int {
        if (remainingSeconds() > 0) return remainingSeconds()
        failures++
        if (failures >= 5) blockedUntil = now() + 60_000
        return remainingSeconds()
    }
    fun reset() { failures = 0; blockedUntil = 0 }
}
