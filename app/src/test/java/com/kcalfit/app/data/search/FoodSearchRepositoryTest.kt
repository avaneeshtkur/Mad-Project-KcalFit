package com.kcalfit.app.data.search

import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.remote.FoodServiceException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FoodSearchRepositoryTest {
    private val rice = FoodItemEntity(id = 42, name = "Rice cooked", barcode = "123", calories = 130, source = "open_food_facts")
    private class Store(var foods: List<FoodItemEntity>) : FoodSearchStore {
        val timestamps = mutableMapOf<String,Long>()
        var writes = 0
        override suspend fun index() = FoodSearchIndex(foods)
        override suspend fun refreshedAt(key: String) = timestamps[key]
        override suspend fun save(key: String, foods: List<FoodItemEntity>, now: Long) {
            writes++
            this.foods = (this.foods + foods).groupBy(FoodSearchIndex::identity).values.map { group -> group.last().copy(id = group.first().id) }
            timestamps[key] = now
        }
        override fun invalidate() { }
    }

    @Test fun localIsImmediateRefreshMergesAndCachesNormalizedQueryFor24Hours() = runTest {
        val store = Store(listOf(rice))
        var now = 1000L
        var requests = 0
        val events = mutableListOf<String>()
        val repository = FoodSearchRepository(store, remote = { requests++; listOf(rice.copy(id = 0, calories = 131)) },
            clock = { now }, diagnostics = FoodSearchDiagnostics { e,_,_,_,_ -> synchronized(events) { events += e } })
        assertEquals(130, repository.local("RIC").foods.single().calories)
        val fresh = repository.refresh(" RICE ")
        assertEquals(131, fresh.foods.single().calories)
        assertEquals(42, fresh.foods.single().id.toInt())
        assertTrue(fresh.fresh)
        repeat(9) { repository.refresh("rice!!") }
        println("FOOD_CACHE_TEST refreshCalls=10 remoteCalls=$requests normalizedReuse=90percent")
        assertEquals(1, requests)
        assertEquals(1, store.writes)
        assertTrue(events.contains("fresh_cache_hit"))
        now += FoodSearchRepository.FRESH_MS + 1
        repository.refresh("rice")
        assertEquals(2, requests)
    }

    @Test fun failureNeverWritesCacheOrDeletesUsableFoodAndEmptyIsCached() = runTest {
        val store = Store(listOf(rice))
        val failing = FoodSearchRepository(store, remote = { throw FoodServiceException(503) })
        try { failing.refresh("rice"); fail("Expected service failure") } catch (_: FoodServiceException) { }
        assertEquals(0, store.writes)
        assertEquals(listOf(rice), failing.local("rice").foods)
        var requests = 0
        val working = FoodSearchRepository(store, remote = { requests++; emptyList() })
        assertTrue(working.refresh("doesnotexist").foods.isEmpty())
        working.refresh("doesnotexist")
        assertEquals(1, requests)
    }
}
