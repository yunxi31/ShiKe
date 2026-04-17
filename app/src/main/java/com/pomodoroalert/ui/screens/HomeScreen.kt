package com.pomodoroalert.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Mic
import androidx.compose.material3.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.viewmodel.HomeViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val tasks by viewModel.tasks.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "任务大厅 - 今日任务列表")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = inputText,
                onValueChange = { viewModel.setInput(it) },
                placeholder = { Text("输入任务名称") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: 语音输入 */ }) {
                Icon(imageVector = Icons.Filled.Mic, contentDescription = "语音输入")
            }
            IconButton(onClick = { /* TODO: 同步日历 */ }) {
                Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "同步日历")
            }
        }
        Button(onClick = { if (inputText.isNotBlank()) viewModel.addTask(inputText) }) {
            Text("添加任务")
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tasks) { task ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = task.taskName)
                            Text(text = "时长: ${task.duration / 60000} 分钟")
                        }
                        Button(onClick = { navController.navigate("focus") /* TODO: 传递 taskId */ }) {
                            Text("开始专注")
                        }
                    }
                }
            }
        }
    }
}

