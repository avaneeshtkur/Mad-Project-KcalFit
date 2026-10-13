package com.kcalfit.app.domain.home

import java.time.ZonedDateTime
import org.junit.Assert.*
import org.junit.Test

class HomePresentationTest {
    @Test fun everyBoundaryUsesRequestedGreeting() {
        mapOf(4 to "Good night",5 to "Good morning",11 to "Good morning",12 to "Good afternoon",
            15 to "Good afternoon",16 to "Good evening",20 to "Good evening",21 to "Good night",0 to "Good night",23 to "Good night")
            .forEach { (h,g) -> assertEquals(g,greetingForHour(h)) }
    }
    @Test fun nextBoundaryIsOneMinuteAway() {
        listOf("04:59","11:59","15:59","20:59").forEach {
            assertEquals(60000,millisUntilGreetingBoundary(ZonedDateTime.parse("2026-10-14T$it:00+05:30[Asia/Kolkata]")))
        }
    }
    @Test fun calorieArcClampsButDoesNotAlterData() {
        assertEquals(0f,calorieRingProgress(0,2000),0f)
        assertEquals(.1f,calorieRingProgress(200,2000),0f)
        assertEquals(.5f,calorieRingProgress(1000,2000),0f)
        assertEquals(1f,calorieRingProgress(2000,2000),0f)
        assertEquals(1f,calorieRingProgress(3000,2000),0f)
        assertEquals(0f,calorieRingProgress(10,0),0f)
    }
}
