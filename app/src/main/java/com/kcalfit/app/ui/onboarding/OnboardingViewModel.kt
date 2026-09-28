package com.kcalfit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kcalfit.app.data.model.UserEntity
import com.kcalfit.app.data.preferences.UserPreferences
import com.kcalfit.app.data.repository.FoodRepository
import com.kcalfit.app.domain.calculator.ActivityLevel
import com.kcalfit.app.domain.calculator.BmrCalculator
import com.kcalfit.app.domain.calculator.FitnessGoal
import com.kcalfit.app.domain.calculator.MacroCalculator
import com.kcalfit.app.domain.calculator.Sex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    private val repository: FoodRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun completeOnboarding(
        name: String,
        age: Int,
        sex: String,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevelName: String,
        goalName: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val sexEnum = if (sex == "FEMALE") Sex.FEMALE else Sex.MALE
                val activityLevel = try {
                    ActivityLevel.valueOf(activityLevelName)
                } catch (e: Exception) {
                    ActivityLevel.MODERATELY_ACTIVE
                }
                val goal = try {
                    FitnessGoal.valueOf(goalName)
                } catch (e: Exception) {
                    FitnessGoal.MAINTAIN
                }

                // Compute BMR, TDEE, and daily calorie target
                val profile = BmrCalculator.computeFullProfile(
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    sex = sexEnum,
                    activityLevel = activityLevel,
                    goal = goal,
                    weeklyChangeKg = 0.5
                )

                // Compute macro targets
                val macros = MacroCalculator.calculateMacros(profile.dailyCalorieTarget)

                // Build updated UserEntity
                val existingUser = repository.getUserProfile()
                val updatedUser = (existingUser ?: UserEntity()).copy(
                    id = 1,
                    name = name,
                    age = age,
                    sex = sex,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    targetWeightKg = targetWeightKg,
                    activityLevel = activityLevelName,
                    fitnessGoal = goalName,
                    dailyCalorieGoal = profile.dailyCalorieTarget,
                    targetProteinGrams = macros.proteinGrams,
                    targetCarbsGrams = macros.carbsGrams,
                    targetFatGrams = macros.fatGrams,
                    isOnboarded = true,
                    isLoggedIn = true
                )

                repository.saveUserProfile(updatedUser)
                userPreferences.setOnboarded(true)
                userPreferences.setCalorieGoal(profile.dailyCalorieTarget)

                _uiState.value = _uiState.value.copy(isSaving = false, isCompleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save profile: ${e.message}"
                )
            }
        }
    }
}

class OnboardingViewModelFactory(
    private val repository: FoodRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
