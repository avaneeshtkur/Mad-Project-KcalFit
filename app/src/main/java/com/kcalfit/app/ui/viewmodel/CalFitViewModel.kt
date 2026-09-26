package com.kcalfit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalfit.app.data.model.FoodEntry
import com.kcalfit.app.data.model.MealType
import com.kcalfit.app.data.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalFitUiState(
    val selectedDate: String = getTodayDateString(),
    val dailyGoal: Int = 2000,
    val selectedDateEntries: List<FoodEntry> = emptyList(),
    val allEntries: List<FoodEntry> = emptyList(),
    val totalCalories: Int = 0,
    val remainingCalories: Int = 2000,
    val isOverGoal: Boolean = false,
    val excessCalories: Int = 0,
    val mealBreakdown: Map<String, Int> = emptyMap(),
    val searchQuery: String = "",
    val selectedMealTypeFilter: String? = null
)

fun getTodayDateString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalFitViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _dailyGoal = MutableStateFlow(repository.getCalorieGoal())
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMealTypeFilter = MutableStateFlow<String?>(null)
    val selectedMealTypeFilter: StateFlow<String?> = _selectedMealTypeFilter.asStateFlow()

    // Flow of entries for selected date
    private val selectedDateEntriesFlow = _selectedDate.flatMapLatest { date ->
        repository.getEntriesForDate(date)
    }

    // Flow of all entries
    val allEntriesFlow = repository.getAllEntries()

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded()
        }
    }

    // Combine dashboard state
    private val dashboardStateFlow = combine(
        _selectedDate,
        _dailyGoal,
        selectedDateEntriesFlow
    ) { date, goal, selectedEntries ->
        Triple(date, goal, selectedEntries)
    }

    // Combine history state
    private val historyStateFlow = combine(
        allEntriesFlow,
        _searchQuery,
        _selectedMealTypeFilter
    ) { allEntries, query, mealFilter ->
        Triple(allEntries, query, mealFilter)
    }

    // Final UI State flow
    val uiState: StateFlow<CalFitUiState> = combine(
        dashboardStateFlow,
        historyStateFlow
    ) { (date, goal, selectedEntries), (allEntries, query, mealFilter) ->

        val totalCalories = selectedEntries.sumOf { it.calories }
        val remaining = goal - totalCalories
        val isOver = totalCalories > goal
        val excess = if (isOver) totalCalories - goal else 0

        val breakdown = mutableMapOf(
            MealType.BREAKFAST.displayName to 0,
            MealType.LUNCH.displayName to 0,
            MealType.DINNER.displayName to 0,
            MealType.SNACK.displayName to 0
        )

        selectedEntries.forEach { entry ->
            val mealName = MealType.fromString(entry.mealType).displayName
            breakdown[mealName] = (breakdown[mealName] ?: 0) + entry.calories
        }

        val filteredAllEntries = allEntries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                    entry.foodName.contains(query, ignoreCase = true) ||
                    entry.mealType.contains(query, ignoreCase = true) ||
                    entry.quantity.contains(query, ignoreCase = true)

            val matchesMeal = mealFilter == null || entry.mealType.equals(mealFilter, ignoreCase = true)

            matchesQuery && matchesMeal
        }

        CalFitUiState(
            selectedDate = date,
            dailyGoal = goal,
            selectedDateEntries = selectedEntries,
            allEntries = filteredAllEntries,
            totalCalories = totalCalories,
            remainingCalories = if (remaining < 0) 0 else remaining,
            isOverGoal = isOver,
            excessCalories = excess,
            mealBreakdown = breakdown,
            searchQuery = query,
            selectedMealTypeFilter = mealFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalFitUiState()
    )

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun updateCalorieGoal(newGoal: Int) {
        if (newGoal > 0) {
            repository.setCalorieGoal(newGoal)
            _dailyGoal.value = newGoal
        }
    }

    fun addFoodEntry(
        foodName: String,
        mealType: String,
        calories: Int,
        quantity: String,
        date: String
    ) {
        viewModelScope.launch {
            val entry = FoodEntry(
                foodName = foodName.trim(),
                mealType = mealType,
                calories = calories,
                quantity = if (quantity.isBlank()) "1 serving" else quantity.trim(),
                date = date
            )
            repository.insertFoodEntry(entry)
        }
    }

    fun deleteFoodEntry(foodEntry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(foodEntry)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMealTypeFilter(mealType: String?) {
        _selectedMealTypeFilter.value = mealType
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded(force = true)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
