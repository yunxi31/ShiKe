package com.pomodoroalert.voice

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import android.text.format.DateUtils
import com.pomodoroalert.data.TaskEntity
import java.util.Calendar

class CalendarManager(private val context: Context) {
    fun fetchTodayEvents(): List<TaskEntity> {
        val events = mutableListOf<TaskEntity>()
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

        val beginTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(beginTime.timeInMillis.toString(), endTime.timeInMillis.toString())

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val titleIdx = it.getColumnIndex(CalendarContract.Events.TITLE)
            val startIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIdx = it.getColumnIndex(CalendarContract.Events.DTEND)

            while (it.moveToNext()) {
                val title = it.getString(titleIdx)
                val start = it.getLong(startIdx)
                val end = it.getLong(endIdx)
                val duration = if (end > start) end - start else 25 * 60_000L

                events.add(
                    TaskEntity(
                        taskName = title ?: "未命名任务",
                        duration = duration,
                        status = "待开始",
                        createdAt = System.currentTimeMillis(),
                        source = "日历"
                    )
                )
            }
        }
        return events
    }
}
