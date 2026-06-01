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
        // 1. Acquire WakeLock with screen wakeup capability to turn on the screen
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            @Suppress("DEPRECATION")
            val wl = pm.newWakeLock(
                android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        android.os.PowerManager.ON_AFTER_RELEASE,
                "PomodoroAlert::ReceiverWakeLock"
            )
            wl.acquire(10000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start TimerService to handle any pending timer
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            putExtra("duration", 0L) // zero indicates alarm trigger
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Direct launch AlarmWakeUpActivity as backup to fullScreenIntent
        val fullScreenIntent = Intent(context, com.pomodoroalert.ui.AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Show high-priority full-screen notification using the alarm channel
        val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }

        val pending = PendingIntent.getActivity(
            context,
            2003,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0),
            bundle
        )
        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)

        val notification = NotificationCompat.Builder(context, "alarm_service_channel_v2")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("番茄钟提醒")
            .setContentText("时间到，请休息")
            .setFullScreenIntent(pending, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
