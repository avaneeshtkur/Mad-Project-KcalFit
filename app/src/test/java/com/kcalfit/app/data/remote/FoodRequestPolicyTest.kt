package com.kcalfit.app.data.remote

import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.domain.calculator.FoodDiaryRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FoodRequestPolicyTest {
    @Test fun unavailableServiceHasExactlyThreeAttemptsWithBackoff() = runTest {
        val attempts = mutableListOf<Long>()
        val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }, jitter = { 25 })
        try {
            policy.execute<String> { attempts += testScheduler.currentTime; throw FoodServiceException(503) }
            fail("Expected 503")
        } catch (e: FoodServiceException) { assertEquals(503, e.status) }
        assertEquals(listOf(0L, 1025L, 3050L), attempts)
    }

    @Test fun retryAfterAndSearchRateGateAreRespected() = runTest {
        val attempts = mutableListOf<Long>()
        val policy = FoodRequestPolicy(clock = { testScheduler.currentTime }, jitter = { 0 })
        var first = true
        val result = policy.execute {
            attempts += testScheduler.currentTime
            if (first) { first = false; throw FoodServiceException(503, 9000) }
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(listOf(0L,9000L), attempts)
        policy.execute { attempts += testScheduler.currentTime; "again" }
        assertEquals(15500L, attempts.last())
        assertEquals(5000L, FoodRequestPolicy.retryAfterMillis("5", 0))
        assertEquals(86401000L, FoodRequestPolicy.retryAfterMillis("86401", 0))
        assertEquals(5000L, FoodRequestPolicy.retryAfterMillis("Thu, 01 Jan 1970 00:00:05 GMT", 0))
    }

    @Test fun authErrorsDoNotRetryAndCancellationStopsDelayedRetry() = runTest {
        var attempts = 0
        val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }, jitter = { 0 })
        try { policy.execute<String> { attempts++; throw FoodServiceException(401) }; fail("Expected error") }
        catch (_: FoodServiceException) { }
        assertEquals(1, attempts)
        val job = launch { policy.execute<String> { attempts++; throw FoodServiceException(503, 10000) } }
        runCurrent()
        job.cancelAndJoin()
        delay(11000)
        assertEquals(2, attempts)
    }

    @Test fun mockHttp503ThenSuccessParsesWithoutChangingNutrientsAndSavesCorrectPortion() = runTest {
        var requests = 0
        val transport = FoodHttpTransport { url ->
            assertTrue(url.contains("search_terms=brown+rice"))
            assertTrue(url.contains("page_size=60"))
            if (++requests == 1) FoodHttpResponse(503, "", "1") else FoodHttpResponse(200, RESPONSE)
        }
        val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }, jitter = { 0 })
        val client = OpenFoodFactsClient(transport, policy, policy, clock = { testScheduler.currentTime })
        val food = client.search("brown rice").single()
        assertEquals(2, requests)
        assertEquals("123", food.barcode)
        assertEquals(130, food.calories)
        assertEquals(100.0, food.servingSize, 0.0)
        assertEquals(2.7, food.proteinGrams, 0.0)
        assertEquals(10.0, food.sodiumMg, 0.001)
        val entry = FoodDiaryRules.fromCatalog(food, 150.0, "150 g", MealType.BREAKFAST, "2026-10-17")
        assertEquals(195, entry.calories)
        assertEquals(4.05, entry.proteinGrams, 0.001)
        assertEquals(42.0, entry.carbsGrams, 0.001)
        assertEquals("Brown rice cooked", entry.foodName)
        assertEquals(195, FoodDiaryRules.totals(listOf(entry)).calories)
    }

    @Test fun invalidResponseIsAnErrorAndMissingMacrosRemainUnavailable() = runTest {
        val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }, jitter = { 0 })
        val invalid = OpenFoodFactsClient(FoodHttpTransport { FoodHttpResponse(200, "{}") }, policy, policy)
        try { invalid.search("rice"); fail("Expected parse error") } catch (_: IllegalStateException) { }
        val missing = RESPONSE.replace("\"fat_100g\":0.3,", "")
        val client = OpenFoodFactsClient(FoodHttpTransport { FoodHttpResponse(200, missing) },
            FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }), policy)
        assertFalse(client.search("rice").single().nutritionKnown)
    }

    @Test fun allTransientStatusesAndConnectivityFailuresAreBounded() = runTest {
        for (status in listOf(429, 500, 502, 503, 504, 0)) {
            var count = 0
            val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime }, jitter = { 0 })
            try {
                policy.execute<String> { count++; if (status == 0) throw IOException("offline") else throw FoodServiceException(status) }
                fail("Expected failure")
            } catch (_: IOException) { }
            assertEquals("status=$status", 3, count)
        }
        for (status in listOf(400, 401, 403, 404)) {
            var count = 0
            val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime })
            try { policy.execute<String> { count++; throw FoodServiceException(status) }; fail("Expected error") }
            catch (_: FoodServiceException) { }
            assertEquals(1, count)
        }
    }

    @Test fun overallBudgetDoesNotIgnoreLongRetryAfter() = runTest {
        var attempts = 0
        val policy = FoodRequestPolicy(minIntervalMillis = 0, clock = { testScheduler.currentTime })
        try {
            policy.execute<String> { attempts++; throw FoodServiceException(503, 60000) }
            fail("Expected timeout")
        } catch (_: java.net.SocketTimeoutException) { }
        assertEquals(1, attempts)
        assertEquals(24000L, testScheduler.currentTime)
    }

    companion object {
        private const val RESPONSE = """{"products":[{"code":"123","product_name":"Brown rice cooked","nutriments":{"energy-kcal_100g":130,"proteins_100g":2.7,"carbohydrates_100g":28,"fat_100g":0.3,"sodium_100g":0.01}}]}"""
    }
}
