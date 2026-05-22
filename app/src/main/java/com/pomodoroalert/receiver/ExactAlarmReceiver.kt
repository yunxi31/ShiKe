package com.pomodoroalert.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pomodoroalert.R
import com.pomodoroalert.service.TimerService

class ExactAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Acquire wake lock
        WakeLockManager.acquire(context)
        // Start TimerService to handle any pending timer (or directly launch Alarm activity)
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            putExtra("duration", 0L) // zero indicates alarm trigger
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        // Show full-screen notification to break lock screen
        val fullScreenIntent = Intent(context, com.pomodoroalert.ui.AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val notification = NotificationCompat.Builder(context, "timer_service_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("番茄钟提醒")
            .setContentText("时间到，请休息")
            .setFullScreenIntent(pending, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        NotificationManagerCompat.from(context).notify(2, notification)
        // Release wake lock after short delay
        WakeLockManager.releaseDelayed(context, 5000L)
    }
}
