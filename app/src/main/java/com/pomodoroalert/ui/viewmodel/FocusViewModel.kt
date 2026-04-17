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

    private val _remainingTime = MutableStateFlow<Long>(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    fun startFocus(taskId: String) {
        viewModelScope.launch {
            val task = taskRepo.getTaskById(taskId) ?: return@launch
            _currentTask.value = task
            _remainingTime.value = task.duration
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
            val task = _currentTask.value ?: return@launch
            val newDuration = minutes * 60_000L
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, com.pomodoroalert.receiver.ExactAlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val triggerAt = System.currentTimeMillis() + newDuration
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            _remainingTime.value = newDuration
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
