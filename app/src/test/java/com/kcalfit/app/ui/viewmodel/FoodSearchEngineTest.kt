package com.kcalfit.app.ui.viewmodel

import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.search.FoodSearchSnapshot
import com.kcalfit.app.data.search.FoodSearchSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodSearchEngineTest {
    private val rice = FoodItemEntity(id = 10, name = "Rice", servingSize = 100.0, servingUnit = "g", calories = 130)
    private class Source : FoodSearchSource {
        var cached = FoodSearchSnapshot(emptyList(), false)
        var requests = 0
        var localCalls = 0
        var fetch: suspend (String) -> FoodSearchSnapshot = { FoodSearchSnapshot(emptyList(), true) }
        override suspend fun local(query: String): FoodSearchSnapshot { localCalls++; return cached }
        override suspend fun refresh(query: String, force: Boolean): FoodSearchSnapshot { requests++; return fetch(query) }
    }

    @Test fun riLoadsThenShowsSuccessfulResultsAfterDebounce() = runTest {
        val source = Source().apply { fetch = { FoodSearchSnapshot(listOf(rice), true) } }
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery("RI")
        assertEquals(FoodSearchUiState.Loading("ri"), engine.state.value)
        runCurrent()
        advanceTimeBy(299); runCurrent()
        assertEquals(0, source.requests)
        advanceTimeBy(1); runCurrent()
        assertEquals(listOf(rice), (engine.state.value as FoodSearchUiState.Results).foods)
        assertEquals(1, source.requests)
    }

    @Test fun cacheIsShownBeforeRemoteAndKeptDuring503() = runTest {
        val source = Source().apply {
            cached = FoodSearchSnapshot(listOf(rice), false)
            fetch = { throw IllegalStateException("Temporary service failure (503)") }
        }
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery("RIC"); runCurrent()
        assertEquals(listOf(rice), (engine.state.value as FoodSearchUiState.Refreshing).foods)
        assertEquals(0, source.requests)
        advanceTimeBy(300); runCurrent()
        val error = engine.state.value as FoodSearchUiState.Error
        assertEquals(listOf(rice), error.cachedFoods)
        assertFalse(engine.state.value is FoodSearchUiState.Empty)
    }

    @Test fun normalizedIdenticalQueriesDoNotResetOrRefetch() = runTest {
        val source = Source().apply { cached = FoodSearchSnapshot(listOf(rice), true) }
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery(" RICE "); runCurrent()
        val result = engine.state.value
        engine.setQuery("rice!!"); runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals(result, engine.state.value)
        assertEquals(1, source.localCalls)
        assertEquals(0, source.requests)
    }

    @Test fun typingBurstProducesOneRequest() = runTest {
        val source = Source()
        val engine = FoodSearchEngine(backgroundScope, source)
        for (query in listOf("R", "RI", "RIC", "RICE")) {
            engine.setQuery(query); runCurrent(); advanceTimeBy(50)
        }
        advanceTimeBy(300); runCurrent()
        assertEquals(1, source.requests)
    }

    @Test fun oldRequestsAreCancelledAndCannotOverwriteNewResults() = runTest {
        var cancelled = false
        val source = Source().apply {
            fetch = { query ->
                if (query == "ri") try { awaitCancellation() } finally { cancelled = true }
                else FoodSearchSnapshot(listOf(rice), true)
            }
        }
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery("RI"); runCurrent(); advanceTimeBy(300); runCurrent()
        engine.setQuery("RICE"); runCurrent()
        assertTrue(cancelled)
        advanceTimeBy(300); runCurrent()
        assertEquals("rice", (engine.state.value as FoodSearchUiState.Results).query)
        engine.setQuery(""); runCurrent()
        assertEquals(FoodSearchUiState.Idle, engine.state.value)
    }

    @Test fun successfulZeroResultsAreEmptyAndRetryCanRecoverFailure() = runTest {
        val source = Source()
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery("RI"); runCurrent(); advanceTimeBy(300); runCurrent()
        assertTrue(engine.state.value is FoodSearchUiState.Empty)
        source.fetch = { throw IllegalStateException("offline") }
        engine.setQuery("RICE"); runCurrent(); advanceTimeBy(300); runCurrent()
        assertTrue(engine.state.value is FoodSearchUiState.Error)
        source.fetch = { FoodSearchSnapshot(listOf(rice), true) }
        engine.retry(); runCurrent(); advanceTimeBy(300); runCurrent()
        assertEquals(listOf(rice), engine.state.value.foods)
    }
    @Test fun nonCooperativeOlderResponseStillCannotOverwriteLatestQuery() = runTest {
        val old = CompletableDeferred<FoodSearchSnapshot>()
        val source = Source().apply {
            fetch = { term ->
                if (term == "ri") kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) { old.await() }
                else FoodSearchSnapshot(listOf(rice), true)
            }
        }
        val engine = FoodSearchEngine(backgroundScope, source)
        engine.setQuery("RI"); runCurrent(); advanceTimeBy(300); runCurrent()
        engine.setQuery("RICE"); runCurrent(); advanceTimeBy(300); runCurrent()
        val latest = engine.state.value
        old.complete(FoodSearchSnapshot(listOf(rice.copy(name = "Old")), true)); runCurrent()
        assertEquals(latest, engine.state.value)
        assertEquals("rice", (latest as FoodSearchUiState.Results).query)
    }
}
