package com.pomodoroalert.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.TaskEntity
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepo: TaskRepository
) : ViewModel() {
    private val _currentTask = MutableStateFlow<TaskEntity?>(null)
    val currentTask: StateFlow<TaskEntity?> = _currentTask.asStateFlow()

    val remainingTime: StateFlow<Long> = com.pomodoroalert.service.TimerState.remainingTime.asStateFlow()

    fun startFocus(taskId: String) {
        viewModelScope.launch {
            if (taskId.startsWith("quick_countdown_")) {
                val durationMs = taskId.substringAfter("quick_countdown_").toLongOrNull() ?: (25 * 60_000L)
                val task = TaskEntity(
                    taskId = taskId,
                    taskName = "快速倒计时",
                    duration = durationMs,
                    status = "进行中",
                    createdAt = System.currentTimeMillis()
                )
                _currentTask.value = task
                com.pomodoroalert.service.TimerState.remainingTime.value = durationMs
                val intent = Intent(context, TimerService::class.java).apply {
                    putExtra("duration", durationMs)
                    putExtra("taskId", taskId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                return@launch
            }

            val task = taskRepo.getTaskById(taskId) ?: return@launch
            _currentTask.value = task
            com.pomodoroalert.service.TimerState.remainingTime.value = task.duration
            val intent = Intent(context, TimerService::class.java).apply {
                putExtra("duration", task.duration)
                putExtra("taskId", taskId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun postpone(minutes: Int) {
        viewModelScope.launch {
            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_POSTPONE
                putExtra("postpone_minutes", minutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun completeTask() {
        viewModelScope.launch {
            val task = _currentTask.value ?: return@launch
            taskRepo.updateStatus(task.taskId, "已完成")
            context.stopService(Intent(context, TimerService::class.java))
            _currentTask.value = null
        }
    }

    fun abandonTask() {
        viewModelScope.launch {
            val task = _currentTask.value ?: return@launch
            taskRepo.updateStatus(task.taskId, "已放弃")
            context.stopService(Intent(context, TimerService::class.java))
            _currentTask.value = null
        }
    }
}
