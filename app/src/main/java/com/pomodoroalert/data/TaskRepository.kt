package com.pomodoroalert.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.pomodoroalert.network.WebhookApi
import com.pomodoroalert.worker.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: AppDatabase,
    private val webhookApi: WebhookApi,
    private val userPrefs: UserPreferences,
    @ApplicationContext private val context: Context
) {
    private val dao = db.taskDao()

    fun getActiveTasks(): Flow<List<TaskEntity>> = dao.getActiveTasks()

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    suspend fun insert(task: TaskEntity) = dao.insert(task)

    suspend fun deleteTask(id: String) = dao.deleteTask(id)

    suspend fun updateStatus(id: String, newStatus: String) {
        if (id.startsWith("quick_countdown_")) return
        dao.updateStatus(id, newStatus)
        // If task is finished (Completed/Abandoned/Postponed), trigger sync
        if (newStatus == "已完成" || newStatus == "已放弃" || newStatus == "推迟") {
            triggerSync(id)
        }
    }

    suspend fun updateTaskStatus(id: String, newStatus: String, completedAt: Long?) {
        if (id.startsWith("quick_countdown_")) return
        dao.updateTaskStatus(id, newStatus, completedAt)
        if (newStatus == "已完成" || newStatus == "已放弃" || newStatus == "推迟") {
            triggerSync(id)
        }
    }

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)

    private fun triggerSync(taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val task = dao.getTaskById(taskId) ?: return@launch
            val voiceId = userPrefs.voiceTone.first()

            val payload = WebhookPayload(
                logId = task.taskId,
                taskName = task.taskName,
                planDuration = (task.duration / 60000).toInt(),
                actualStatus = when (task.status) {
                    "已完成" -> "Completed"
                    "已放弃" -> "Abandoned"
                    "推迟" -> "Postponed"
                    else -> "Completed"
                },
                triggerSource = when (task.source) {
                    "手动" -> "Manual"
                    "语音" -> "Voice"
                    "日历" -> "Calendar"
                    else -> "Manual"
                },
                startTime = formatTimestamp(task.createdAt),
                endTime = formatTimestamp(System.currentTimeMillis()),
                voiceId = voiceId
            )

            try {
                val response = webhookApi.syncTask(payload = payload)
                if (response.isSuccessful) {
                    dao.updateSyncStatus(task.taskId, "Synced")
                } else {
                    markPendingAndScheduleRetry(task.taskId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                markPendingAndScheduleRetry(task.taskId)
            }
        }
    }

    private suspend fun markPendingAndScheduleRetry(taskId: String) {
        dao.updateSyncStatus(taskId, "Sync_Pending")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
