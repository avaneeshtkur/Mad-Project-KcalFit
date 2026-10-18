package com.kcalfit.app.domain.health
import com.kcalfit.app.data.health.stepCounterDelta
import org.junit.Assert.assertEquals
import org.junit.Test
class StepCounterDeltaTest {
    @Test fun sensorBatchCountsOnlyNewSteps(){assertEquals(42L,stepCounterDelta(1000,1042,true))}
    @Test fun repeatedSensorValueDoesNotDuplicate(){assertEquals(0L,stepCounterDelta(1042,1042,true))}
    @Test fun rebootDoesNotProduceNegativeSteps(){assertEquals(0L,stepCounterDelta(1042,3,true))}
    @Test fun midnightDoesNotAssignYesterdayToToday(){assertEquals(0L,stepCounterDelta(1042,1100,false))}
}
