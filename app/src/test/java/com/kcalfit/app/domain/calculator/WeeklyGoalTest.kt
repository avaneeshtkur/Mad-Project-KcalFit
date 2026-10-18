package com.kcalfit.app.domain.calculator
import org.junit.Assert.assertEquals
import org.junit.Test
class WeeklyGoalTest {
    @Test fun suppliedPointTwoGainUsesPointTwoAdjustment(){assertEquals(2200,BmrCalculator.calculateDailyTarget(2000,FitnessGoal.GAIN_WEIGHT,.2).dailyCalorieTarget)}
    @Test fun maintainDoesNotAddWeeklySurplus(){assertEquals(2000,BmrCalculator.calculateDailyTarget(2000,FitnessGoal.MAINTAIN,.5).dailyCalorieTarget)}
    @Test fun existingHalfKiloDefaultIsPreserved(){assertEquals(1500,BmrCalculator.calculateDailyTarget(2000,FitnessGoal.LOSE_WEIGHT).dailyCalorieTarget)}
}
