package com.pomodoroalert.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pomodoroalert.ui.screens.HomeScreen
import com.pomodoroalert.ui.screens.FocusScreen
import com.pomodoroalert.ui.screens.StatsScreen
import com.pomodoroalert.ui.screens.SettingsScreen
import com.pomodoroalert.ui.screens.AlarmScreen
import com.pomodoroalert.ui.screens.SplashScreen
import com.pomodoroalert.ui.viewmodel.SettingsViewModel
import com.pomodoroalert.ui.localization.ProvideLocalization

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val language by settingsViewModel.language.collectAsState()

    ProvideLocalization(language = language) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") { HomeScreen(navController) }
            composable(
                route = "focus?taskId={taskId}",
                arguments = listOf(
                    androidx.navigation.navArgument("taskId") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                FocusScreen(navController, taskId)
            }
            composable("stats") { StatsScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("alarm") { AlarmScreen(navController) }
        }
    }
}
