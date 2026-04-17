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

    @Query("SELECT * FROM tasks WHERE status != '已放弃' ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE taskId = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("UPDATE tasks SET status = :newStatus WHERE taskId = :id")
    suspend fun updateStatus(id: String, newStatus: String)

    @Query("SELECT * FROM tasks WHERE sync_status = 'Sync_Pending'")
    suspend fun getPendingSyncTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET sync_status = :syncStatus WHERE taskId = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)
}
