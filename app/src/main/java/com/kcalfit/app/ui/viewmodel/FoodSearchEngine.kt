package com.kcalfit.app.ui.viewmodel

import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.search.FoodSearchIndex
import com.kcalfit.app.data.search.FoodSearchSource
import com.kcalfit.app.data.search.FoodSearchText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FoodSearchUiState {
    data object Idle : FoodSearchUiState
    data class Loading(val query: String) : FoodSearchUiState
    data class Refreshing(val query: String, val foods: List<FoodItemEntity>) : FoodSearchUiState
    data class Results(val query: String, val foods: List<FoodItemEntity>, val fromCache: Boolean = false) : FoodSearchUiState
    data class Empty(val query: String) : FoodSearchUiState
    data class Error(val query: String, val message: String, val cachedFoods: List<FoodItemEntity> = emptyList()) : FoodSearchUiState
}

val FoodSearchUiState.foods: List<FoodItemEntity> get() = when (this) {
    is FoodSearchUiState.Results -> foods
    is FoodSearchUiState.Refreshing -> foods
    is FoodSearchUiState.Error -> cachedFoods
    else -> emptyList()
}

/** Local lookup is immediate; only the remote refresh is debounced. */
class FoodSearchEngine(
    private val scope: CoroutineScope,
    private val source: FoodSearchSource,
    private val debounceMillis: Long = 300
) {
    private val _state = MutableStateFlow<FoodSearchUiState>(FoodSearchUiState.Idle)
    val state: StateFlow<FoodSearchUiState> = _state.asStateFlow()
    private var query = ""
    private var generation = 0L
    private var work: Job? = null

    fun setQuery(value: String) {
        val normalized = FoodSearchText.normalize(value)
        if (normalized == query) return
        query = normalized
        start(force = false)
    }

    fun retry() {
        if (query.isNotBlank() && work?.isActive != true) start(force = true)
    }

    private fun start(force: Boolean) {
        work?.cancel()
        val request = ++generation
        val term = query
        if (term.isBlank()) { _state.value = FoodSearchUiState.Idle; return }
        var available = FoodSearchIndex(_state.value.foods).search(term)
        _state.value = pending(term, available)
        work = scope.launch {
            try {
                val cached = source.local(term)
                if (request != generation) return@launch
                available = cached.foods
                if (cached.fresh && !force) {
                    _state.value = complete(term, available, fromCache = true)
                    return@launch
                }
                _state.value = pending(term, available)
                delay(debounceMillis)
                val refreshed = source.refresh(term, force)
                if (request == generation) _state.value = complete(term, refreshed.foods, fromCache = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (request == generation) _state.value = FoodSearchUiState.Error(term,
                    failure.message ?: "Food search is unavailable. Please retry shortly.", available)
            }
        }
    }

    private fun pending(query: String, foods: List<FoodItemEntity>): FoodSearchUiState =
        if (foods.isEmpty()) FoodSearchUiState.Loading(query) else FoodSearchUiState.Refreshing(query, foods)
    private fun complete(query: String, foods: List<FoodItemEntity>, fromCache: Boolean): FoodSearchUiState =
        if (foods.isEmpty()) FoodSearchUiState.Empty(query) else FoodSearchUiState.Results(query, foods, fromCache)
}
