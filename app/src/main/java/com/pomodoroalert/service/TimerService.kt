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
import com.pomodoroalert.MainActivity
import com.pomodoroalert.ui.AlarmWakeUpActivity
import com.pomodoroalert.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object TimerState {
    val remainingTime = MutableStateFlow(0L)
}

class TimerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var voiceManager: VoiceManager? = null
    private var pendingTaskId: String? = null

    private val channelId = "timer_service_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification(0L))
        voiceManager = VoiceManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val duration = intent?.getLongExtra("duration", 0L) ?: 0L
        pendingTaskId = intent?.getStringExtra("taskId")
        if (duration > 0) {
            startTimer(duration)
        }
        return START_STICKY
    }

    private fun startTimer(durationMs: Long) {
        serviceScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                TimerState.remainingTime.emit(remaining)
                updateNotification(remaining)
                delay(1000L)
                remaining -= 1000L
            }
            TimerState.remainingTime.emit(0L)
            // 时间到，触发播报+闹钟（Service 由 AlarmWakeUpActivity 停止）
            triggerAlarm()
        }
    }

    private fun triggerAlarm() {
        // 先让Service里的VoiceManager播报（生命周期不受Activity影响）
        voiceManager?.speak("威哥，本次专注已经结束，你超棒哦")
        val alarmIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            pendingTaskId?.let { putExtra("taskId", it) }
        }
        startActivity(alarmIntent)
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
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

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext[Job]?.cancel()
        // voiceManager 让它自然播完，不提前 abandon
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
