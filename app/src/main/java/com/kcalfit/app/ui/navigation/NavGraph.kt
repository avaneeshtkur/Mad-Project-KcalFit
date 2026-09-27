package com.kcalfit.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kcalfit.app.ui.screens.AddFoodScreen
import com.kcalfit.app.ui.screens.HistoryScreen
import com.kcalfit.app.ui.screens.HomeScreen
import com.kcalfit.app.ui.screens.NutritionScreen
import com.kcalfit.app.ui.viewmodel.CalFitViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: CalFitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        modifier = modifier
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onAddFoodClick = {
                        navController.navigate(Screen.AddFood.route)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    },
                    onUpdateGoal = { newGoal ->
                        viewModel.updateCalorieGoal(newGoal)
                    },
                    onMealCategoryClick = { meal ->
                        viewModel.setMealTypeFilter(meal.displayName)
                        navController.navigate(Screen.Nutrition.route)
                    }
                )
            }

            // 2. Add Food Screen
            composable(Screen.AddFood.route) {
                AddFoodScreen(
                    onAddFoodSubmit = { foodName, mealType, calories, quantity, date ->
                        viewModel.addFoodEntry(
                            foodName = foodName,
                            mealType = mealType,
                            calories = calories,
                            quantity = quantity,
                            date = date
                        )
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 3. Nutrition Summary Screen
            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    uiState = uiState,
                    onDateSelected = { date ->
                        viewModel.setSelectedDate(date)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    }
                )
            }

            // 4. Food History Screen
            composable(Screen.History.route) {
                HistoryScreen(
                    uiState = uiState,
                    onSearchQueryChange = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    onMealFilterChange = { mealFilter ->
                        viewModel.setMealTypeFilter(mealFilter)
                    },
                    onDeleteEntry = { entry ->
                        viewModel.deleteFoodEntry(entry)
                    },
                    onSeedSampleData = {
                        viewModel.seedSampleData()
                    }
                )
            }
        }
    }
}
