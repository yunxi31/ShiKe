package com.pomodoroalert.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatsRepository(private val taskDao: TaskDao) {
    fun getCompletedTasksCount(): Flow<Int> {
        return taskDao.getActiveTasks().map { tasks ->
            tasks.count { it.status == "已完成" }
        }
    }

    fun getTodayCompletedPomodoros(): Flow<Int> {
        // Simple logic for V1: 1 task = 1 pomodoro for demo purposes
        // In a real app, this would track actual timer completion counts
        return getCompletedTasksCount()
    }
}
