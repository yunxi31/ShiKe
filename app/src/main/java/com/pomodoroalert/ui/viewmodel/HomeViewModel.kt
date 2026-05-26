package com.pomodoroalert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.TaskEntity
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.data.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar

enum class TodoFilter {
    ALL, TODAY, IMPORTANT, COMPLETED
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val configRepo: ConfigRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TodoFilter.ALL)
    val currentFilter: StateFlow<TodoFilter> = _currentFilter.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _rawTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val rawTasks: StateFlow<List<TaskEntity>> = _rawTasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepo.getAllTasks().collect { list ->
                _rawTasks.value = list
            }
        }
    }

    val tasks: StateFlow<List<TaskEntity>> = combine(_rawTasks, _currentFilter, _selectedDate) { rawList, filter, selDate ->
        val onSelectedDay = rawList.filter { 
            it.dueDate?.let { d -> isSameDay(d, selDate) } ?: isSameDay(it.createdAt, selDate)
        }
        when (filter) {
            TodoFilter.ALL -> onSelectedDay.filter { it.status != "已放弃" }
            TodoFilter.TODAY -> onSelectedDay.filter { it.status == "待开始" }
            TodoFilter.IMPORTANT -> onSelectedDay.filter { it.status == "进行中" }
            TodoFilter.COMPLETED -> onSelectedDay.filter { it.status == "已完成" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun setInput(text: String) {
        _inputText.value = text
    }

    fun addTask(name: String) {
        addTask(name, priority = 0, dueDate = null, durationMinutes = null)
    }

    fun addTask(name: String, priority: Int, dueDate: Long?, durationMinutes: Long?) {
        viewModelScope.launch {
            val minutes = durationMinutes ?: 25L
            val task = TaskEntity(
                taskName = name,
                duration = minutes * 60_000L,
                status = "待开始",
                createdAt = System.currentTimeMillis(),
                priority = priority,
                dueDate = dueDate
            )
            taskRepo.insert(task)
            _inputText.value = ""
        }
    }

    fun toggleTaskStatus(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val newStatus = if (isCompleted) "已完成" else "待开始"
            val completedTime = if (isCompleted) System.currentTimeMillis() else null
            taskRepo.updateTaskStatus(taskId, newStatus, completedTime)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepo.deleteTask(taskId)
        }
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
