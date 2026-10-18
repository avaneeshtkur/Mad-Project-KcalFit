package com.kcalfit.app.data.search

import com.kcalfit.app.data.local.FoodItemDao
import com.kcalfit.app.data.model.FoodItemEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class RoomFoodSearchStore(private val dao: FoodItemDao, private val clock: () -> Long = System::currentTimeMillis) : FoodSearchStore {
    private val lock = Mutex()
    private val revision = AtomicLong()
    @Volatile private var snapshot: FoodSearchIndex? = null
    private var loadedAt = 0L
    init {
        // Includes direct Room clears/account changes; never retain a prior catalog snapshot.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            dao.observeCatalogChanges().collect { invalidate() }
        }
    }
    override suspend fun index(): FoodSearchIndex = lock.withLock {
        snapshot?.takeIf { clock() - loadedAt in 0 until FoodSearchRepository.FRESH_MS } ?: run {
            if (dao.pruneRemoteFoods(clock() - FoodSearchRepository.RETAIN_MS, FoodSearchRepository.MAX_REMOTE_FOODS) > 0) dao.clearSearchKeys()
            val version = revision.get()
            FoodSearchIndex(dao.searchCatalog()).also {
                if (version == revision.get()) { snapshot = it; loadedAt = clock() }
            }
        }
    }
    override suspend fun refreshedAt(key: String): Long? = dao.searchRefreshedAt(key)
    override suspend fun save(key: String, foods: List<FoodItemEntity>, now: Long) = lock.withLock {
        dao.cacheSearch(key, foods, now, now - FoodSearchRepository.RETAIN_MS,
            FoodSearchRepository.MAX_REMOTE_FOODS, FoodSearchRepository.MAX_QUERY_KEYS)
        snapshot = null
    }
    override fun invalidate() { revision.incrementAndGet(); snapshot = null }
}
