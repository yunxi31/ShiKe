package com.pomodoroalert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepo: StatsRepository
) : ViewModel() {
    val completedTasks: StateFlow<Int> = statsRepo.getCompletedTasksCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedPomodoros: StateFlow<Int> = statsRepo.getTodayCompletedPomodoros()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
