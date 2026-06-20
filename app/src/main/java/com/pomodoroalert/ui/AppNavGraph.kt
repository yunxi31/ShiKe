package com.pomodoroalert.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pomodoroalert.ui.screens.HomeScreen
import com.pomodoroalert.ui.screens.FocusScreen
import com.pomodoroalert.ui.screens.StatsScreen
import com.pomodoroalert.ui.screens.SettingsScreen
import com.pomodoroalert.ui.screens.AlarmScreen
import com.pomodoroalert.ui.screens.SplashScreen
import com.pomodoroalert.ui.screens.ScheduleScreen
import com.pomodoroalert.ui.screens.AlarmDetailScreen
import com.pomodoroalert.ui.viewmodel.SettingsViewModel
import com.pomodoroalert.ui.localization.ProvideLocalization

import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6C5DD3),
    background = Color(0xFFF7F8FC),
    surface = Color.White,
    onBackground = Color(0xFF1B1D21),
    onSurface = Color(0xFF1B1D21),
    onSurfaceVariant = Color(0xFF808191),
    secondaryContainer = Color(0xFFF7F8FC),
    surfaceVariant = Color(0xFFF0EFFC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8C7CFF),
    background = Color(0xFF12121A),
    surface = Color(0xFF1C1D30),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF9EA0B6),
    secondaryContainer = Color(0xFF161622),
    surfaceVariant = Color(0xFF2C2D4E)
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val language by settingsViewModel.language.collectAsState()
    val darkMode by settingsViewModel.darkMode.collectAsState()

    MaterialTheme(
        colorScheme = if (darkMode) DarkColorScheme else LightColorScheme
    ) {
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
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        onToggleDarkMode = settingsViewModel::toggleDarkMode,
                        isDarkMode = darkMode
                    )
                }
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
                composable("schedule") { ScheduleScreen(navController) }
                composable(
                    route = "alarm_detail/{alarmId}",
                    arguments = listOf(
                        androidx.navigation.navArgument("alarmId") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val alarmId = backStackEntry.arguments?.getString("alarmId") ?: ""
                    AlarmDetailScreen(navController, alarmId)
                }
            }
        }
    }
}
