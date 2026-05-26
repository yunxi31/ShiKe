package com.pomodoroalert.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE status NOT IN ('已放弃', '已完成') ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = '已完成'")
    fun getCompletedTasksCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE taskId = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("UPDATE tasks SET status = :newStatus WHERE taskId = :id")
    suspend fun updateStatus(id: String, newStatus: String)

    @Query("UPDATE tasks SET status = :newStatus, completedAt = :completedAt WHERE taskId = :id")
    suspend fun updateTaskStatus(id: String, newStatus: String, completedAt: Long?)

    @Query("SELECT * FROM tasks ORDER BY status ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks WHERE taskId = :id")
    suspend fun deleteTask(id: String)
}
