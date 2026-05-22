package com.pomodoroalert.data

import android.content.ContentResolver
import android.content.Context
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val configRepository: ConfigRepository
) {
    suspend fun syncTodayEvents(): Int = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val calendar = Calendar.getInstance()
        
        // Today range: from start of day to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startMillis = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endMillis = calendar.timeInMillis
        
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        
        val cursor = resolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        
        var importedCount = 0
        val defaultDuration = configRepository.getDefaultPomodoro() * 60_000L
        
        cursor?.use {
            val titleIdx = it.getColumnIndex(CalendarContract.Events.TITLE)
            val startIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIdx = it.getColumnIndex(CalendarContract.Events.DTEND)
            
            while (it.moveToNext()) {
                val title = it.getString(titleIdx) ?: "未命名日程"
                val eventStart = it.getLong(startIdx)
                val eventEnd = it.getLong(endIdx)
                val duration = if (eventEnd > eventStart) (eventEnd - eventStart) else defaultDuration
                
                // Avoid duplicates: simplified by name and date for now
                // In a real app, you might use event ID
                val task = TaskEntity(
                    taskName = title,
                    duration = duration.coerceAtLeast(60_000L), // at least 1 min
                    status = "待开始",
                    createdAt = System.currentTimeMillis(),
                    source = "日历"
                )
                
                taskRepository.insert(task)
                importedCount++
            }
        }
        
        importedCount
    }
}
