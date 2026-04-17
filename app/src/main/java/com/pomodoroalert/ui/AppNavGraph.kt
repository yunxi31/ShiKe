package com.pomodoroalert.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pomodoroalert.ui.screens.HomeScreen
import com.pomodoroalert.ui.screens.FocusScreen
import com.pomodoroalert.ui.screens.StatsScreen
import com.pomodoroalert.ui.screens.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("focus") { FocusScreen(navController) }
        composable("stats") { StatsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
