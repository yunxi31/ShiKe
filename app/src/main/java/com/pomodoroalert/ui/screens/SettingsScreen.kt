package com.pomodoroalert.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val earphoneMode by viewModel.earphoneMode.collectAsState()
    val defaultPomodoro by viewModel.defaultPomodoro.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "偏好设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("耳机模式", style = MaterialTheme.typography.titleMedium)
                    Text("开启后仅在耳机内播报", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = earphoneMode, onCheckedChange = { viewModel.setEarphoneMode(it) })
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("默认专注时长 (分钟)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = defaultPomodoro.toFloat(),
                    onValueChange = { viewModel.setDefaultPomodoro(it.toInt()) },
                    valueRange = 5f..60f,
                    steps = 11 // 5, 10, 15, ..., 60
                )
                Text("当前设置: $defaultPomodoro 分钟", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { navController.popBackStack() }) {
            Text("返回首页")
        }
    }
}
