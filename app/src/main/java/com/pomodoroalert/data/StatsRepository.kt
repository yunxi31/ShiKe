package com.pomodoroalert.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatsRepository(private val taskDao: TaskDao) {
    fun getCompletedTasksCount(): Flow<Int> {
        return taskDao.getCompletedTasksCount()
    }

    fun getTodayCompletedPomodoros(): Flow<Int> {
        // V1 逻辑：每个完成的任务计为一个番茄钟
        return getCompletedTasksCount()
    }
}
