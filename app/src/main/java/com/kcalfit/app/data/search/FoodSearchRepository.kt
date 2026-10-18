package com.kcalfit.app.data.search

import com.kcalfit.app.data.model.FoodItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class FoodSearchSnapshot(val foods: List<FoodItemEntity>, val fresh: Boolean)

interface FoodSearchSource {
    suspend fun local(query: String): FoodSearchSnapshot
    suspend fun refresh(query: String, force: Boolean = false): FoodSearchSnapshot
}

interface FoodSearchStore {
    suspend fun index(): FoodSearchIndex
    suspend fun refreshedAt(key: String): Long?
    suspend fun save(key: String, foods: List<FoodItemEntity>, now: Long)
    fun invalidate()
}

fun interface FoodSearchDiagnostics {
    /** No query text, product names, credentials, or user identifiers. */
    fun record(event: String, durationMs: Long, count: Int, status: Int, retry: Int)
    companion object { val NONE = FoodSearchDiagnostics { _, _, _, _, _ -> } }
}

class FoodSearchRepository(
    private val store: FoodSearchStore,
    private val remote: suspend (String) -> List<FoodItemEntity>,
    private val clock: () -> Long = System::currentTimeMillis,
    private val diagnostics: FoodSearchDiagnostics = FoodSearchDiagnostics.NONE
) : FoodSearchSource {
    private val refreshLock = Mutex()

    override suspend fun local(query: String): FoodSearchSnapshot = withContext(Dispatchers.Default) {
        val start = System.nanoTime()
        val key = FoodSearchText.canonical(query)
        if (key.isBlank()) return@withContext FoodSearchSnapshot(emptyList(), false)
        val foods = store.index().search(query)
        val fetchedAt = store.refreshedAt(key)
        val fresh = fetchedAt != null && clock() - fetchedAt in 0 until FRESH_MS
        diagnostics.record(if (fresh) "fresh_cache_hit" else "local_lookup", (System.nanoTime() - start) / 1_000_000, foods.size, 0, 0)
        FoodSearchSnapshot(foods, fresh)
    }

    override suspend fun refresh(query: String, force: Boolean): FoodSearchSnapshot = refreshLock.withLock {
        val key = FoodSearchText.canonical(query)
        val cached = local(query)
        if (key.isBlank() || (!force && cached.fresh)) return@withLock cached
        val foods = remote(key)
        // A failed request never updates freshness or deletes the usable catalog.
        store.save(key, foods, clock())
        local(query)
    }

    fun invalidate() = store.invalidate()

    companion object {
        const val FRESH_MS = 24 * 60 * 60 * 1000L
        const val RETAIN_MS = 30 * FRESH_MS
        const val MAX_REMOTE_FOODS = 2_000
        const val MAX_QUERY_KEYS = 100
    }
}
