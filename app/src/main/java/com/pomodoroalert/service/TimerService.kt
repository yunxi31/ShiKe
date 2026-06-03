package com.pomodoroalert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pomodoroalert.R
import com.pomodoroalert.MainActivity
import com.pomodoroalert.ui.AlarmWakeUpActivity
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
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_POSTPONE = "ACTION_POSTPONE"
        private const val TAG = "TimerService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var pendingTaskId: String? = null

    private val channelId = "timer_service_channel"
    private val notificationId = 1

    private var timerJob: kotlinx.coroutines.Job? = null
    private var remainingTimeMs = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                buildNotification(0L),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(notificationId, buildNotification(0L))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_POSTPONE) {
            val minutes = intent.getIntExtra("postpone_minutes", 0)
            if (minutes > 0) {
                remainingTimeMs += minutes * 60_000L
                serviceScope.launch {
                    TimerState.remainingTime.emit(remainingTimeMs)
                    updateNotification(remainingTimeMs)
                }
            }
        } else {
            val duration = intent?.getLongExtra("duration", 0L) ?: 0L
            pendingTaskId = intent?.getStringExtra("taskId")
            if (duration > 0) {
                startTimer(duration)
            }
        }
        return START_STICKY
    }

    private fun startTimer(durationMs: Long) {
        timerJob?.cancel()
        remainingTimeMs = durationMs
        timerJob = serviceScope.launch {
            while (remainingTimeMs > 0) {
                TimerState.remainingTime.emit(remainingTimeMs)
                updateNotification(remainingTimeMs)
                delay(1000L)
                remainingTimeMs -= 1000L
            }
            TimerState.remainingTime.emit(0L)
            triggerAlarm()
        }
    }

    private fun triggerAlarm() {
        playAlertSound()
        val alarmIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            pendingTaskId?.let { putExtra("taskId", it) }
        }
        val creatorOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic().apply {
                setPendingIntentCreatorBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            }.toBundle()
        } else {
            null
        }
        val pendingDirect = PendingIntent.getActivity(
            this,
            2006,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0),
            creatorOptions
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val senderOptions = android.app.ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }.toBundle()
                pendingDirect.send(this, 0, null, null, null, null, senderOptions)
            } else {
                pendingDirect.send()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AlarmWakeUpActivity via PendingIntent from TimerService", e)
            try {
                startActivity(alarmIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to start AlarmWakeUpActivity directly from TimerService fallback", ex)
            }
        }
    }

    private fun playAlertSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                val afd = assets.openFd("alert.mp3")
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alert sound: ${e.message}", e)
        }
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
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
