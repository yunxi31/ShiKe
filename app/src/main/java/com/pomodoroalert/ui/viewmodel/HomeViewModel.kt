package com.pomodoroalert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.TaskEntity
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.data.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val configRepo: ConfigRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepo.getActiveTasks().collect { list ->
                _tasks.value = list
            }
        }
    }

    fun setInput(text: String) {
        _inputText.value = text
    }

    fun addTask(name: String) {
        viewModelScope.launch {
            val defaultDuration = configRepo.getDefaultPomodoro()
            val task = TaskEntity(
                taskName = name,
                duration = defaultDuration * 60_000L,
                status = "待开始",
                createdAt = System.currentTimeMillis(),
                source = "手动"
            )
            taskRepo.insert(task)
            _inputText.value = ""
        }
    }
}
