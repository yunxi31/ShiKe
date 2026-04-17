package com.pomodoroalert.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.viewmodel.FocusViewModel

@Composable
fun FocusScreen(navController: NavController, taskId: String? = null) {
    val viewModel: FocusViewModel = hiltViewModel()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.startFocus(taskId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "专注面板", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = currentTask?.taskName ?: "未选择任务", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        val seconds = (remainingTime / 1000) % 60
        val minutes = (remainingTime / 1000) / 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(64.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                viewModel.completeTask()
                navController.popBackStack()
            }) {
                Text("完成")
            }
            Button(onClick = { viewModel.postpone(10) }) {
                Text("推迟10分钟")
            }
            Button(
                onClick = {
                    viewModel.abandonTask()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("放弃")
            }
        }
    }
}
