package com.kcalfit.app.domain.auth

import org.junit.Assert.*
import org.junit.Test

class LoginThrottleTest {
    @Test fun fifthFailureBlocksForSixtySeconds() {
        var time = 1000L
        val throttle = LoginThrottle(now = { time })
        repeat(4) { assertEquals(0, throttle.failed()) }
        assertEquals(60, throttle.failed())
        time += 59_001
        assertEquals(1, throttle.remainingSeconds())
        time += 999
        assertEquals(0, throttle.remainingSeconds())
        assertEquals(0, throttle.failures)
        assertEquals(0, throttle.failed())
    }
    @Test fun restorationKeepsCooldownAndBlockedAttemptsDoNotExtendIt() {
        var time = 1000L
        val original = LoginThrottle(now = { time })
        repeat(5) { original.failed() }
        time += 10_000
        val restored = LoginThrottle(original.failures, original.blockedUntil) { time }
        assertEquals(50, restored.failed())
        assertEquals(original.blockedUntil, restored.blockedUntil)
    }
    @Test fun successfulLoginResetsFailures() {
        val throttle = LoginThrottle()
        repeat(4) { throttle.failed() }
        throttle.reset()
        assertEquals(0, throttle.failed())
        assertEquals(1, throttle.failures)
    }
}
