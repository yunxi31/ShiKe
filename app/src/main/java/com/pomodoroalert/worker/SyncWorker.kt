package com.pomodoroalert.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pomodoroalert.data.AppDatabase
import com.pomodoroalert.data.UserPreferences
import com.pomodoroalert.data.WebhookPayload
import com.pomodoroalert.network.WebhookApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: AppDatabase,
    private val webhookApi: WebhookApi,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = db.taskDao()
        val pendingTasks = dao.getPendingSyncTasks()

        if (pendingTasks.isEmpty()) {
            return Result.success()
        }

        val voiceId = userPreferences.voiceTone.first()

        var allSuccess = true
        for (task in pendingTasks) {
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
                endTime = formatTimestamp(System.currentTimeMillis()), // Since this might be a retry, endTime might be inaccurate if not stored. Ideally stored in DB. Using current for simplicity or we can add endTime to entity later.
                voiceId = voiceId
            )

            try {
                val response = webhookApi.syncTask(payload = payload)
                if (response.isSuccessful) {
                    dao.updateSyncStatus(task.taskId, "Synced")
                } else {
                    allSuccess = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
