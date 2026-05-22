package com.pomodoroalert.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val taskId: String = UUID.randomUUID().toString(),
    val taskName: String,
    val duration: Long, // ms
    val status: String, // 待开始/进行中/已完成/已放弃
    val createdAt: Long,
    val source: String, // 手动/语音/日历
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "Synced", // Synced, Sync_Pending
    val priority: Int = 0, // 0 = 无, 1 = 低, 2 = 中, 3 = 高
    val dueDate: Long? = null,
    val completedAt: Long? = null
)
