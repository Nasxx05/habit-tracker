package com.habitstreak.app

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habitstreak.app.ui.screens.*
import com.habitstreak.app.ui.theme.HabitStreakTheme
import com.habitstreak.app.viewmodel.HabitViewModel

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Main : Screen("main")
    data object AddHabit : Screen("add_habit")
    data object Calendar : Screen("calendar")
    data object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: String) = "edit_habit/$habitId"
    }
}

@Composable
fun HabitStreakApp(modifier: Modifier = Modifier) {
    val viewModel: HabitViewModel = viewModel()
    val navController = rememberNavController()

    val startDestination = if (viewModel.isFirstLaunch) Screen.Welcome.route else Screen.Welcome.route

    HabitStreakTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    viewModel = viewModel,
                    isFirstLaunch = viewModel.isFirstLaunch,
                    onContinue = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Main.route) {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToAddHabit = {
                        navController.navigate(Screen.AddHabit.route)
                    },
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route)
                    },
                    onNavigateToEditHabit = { habitId ->
                        navController.navigate(Screen.EditHabit.createRoute(habitId))
                    }
                )
            }

            composable(Screen.AddHabit.route) {
                AddHabitScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditHabit.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
                EditHabitScreen(
                    viewModel = viewModel,
                    habitId = habitId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
