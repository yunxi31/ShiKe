package com.pomodoroalert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pomodoroalert.R
import com.pomodoroalert.ui.AlarmWakeUpActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val _remainingTime = MutableStateFlow<Long>(0L)
    val remainingTime: StateFlow<Long> = _remainingTime

    private val channelId = "timer_service_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification(0L))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getLongExtra("duration", 0L) ?: 0L
        if (duration > 0) {
            startTimer(duration)
        }
        return START_STICKY
    }

    private fun startTimer(durationMs: Long) {
        serviceScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                _remainingTime.emit(remaining)
                updateNotification(remaining)
                delay(1000L)
                remaining -= 1000L
            }
            // 时间到，触发闹钟
            triggerAlarm()
            stopSelf()
        }
    }

    private fun triggerAlarm() {
        val alarmIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(alarmIntent)
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.pomodoroalert.ui.MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("番茄钟运行中")
            .setContentText("剩余时间: ${remainingMs / 1000}s")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, buildNotification(remainingMs))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "番茄钟前台服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
