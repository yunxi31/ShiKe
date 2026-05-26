package com.pomodoroalert.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: AppDatabase
) {
    private val dao = db.taskDao()

    fun getActiveTasks(): Flow<List<TaskEntity>> = dao.getActiveTasks()

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    suspend fun insert(task: TaskEntity) = dao.insert(task)

    suspend fun deleteTask(id: String) = dao.deleteTask(id)

    suspend fun updateStatus(id: String, newStatus: String) {
        if (id.startsWith("quick_countdown_")) return
        dao.updateStatus(id, newStatus)
    }

    suspend fun updateTaskStatus(id: String, newStatus: String, completedAt: Long?) {
        if (id.startsWith("quick_countdown_")) return
        dao.updateTaskStatus(id, newStatus, completedAt)
    }

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)
}
