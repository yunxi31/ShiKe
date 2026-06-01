package com.pomodoroalert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pomodoroalert.MainActivity
import com.pomodoroalert.R
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.ConfigRepository
import com.pomodoroalert.ui.AlarmWakeUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        private const val TAG = "AlarmService"
        private const val CHANNEL_ID = "alarm_service_channel"
        private const val NOTIFICATION_ID = 3001
    }

    @Inject lateinit var configRepo: ConfigRepository
    @Inject lateinit var alarmDao: AlarmDao

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra("alarmId")
        val remark = intent?.getStringExtra("alarmRemark") ?: "闹钟时间到了！"
        val ringtoneUri = intent?.getStringExtra("ringtoneUri")

        // 1. 立即在前台启动服务以满足 Android 12+ 后台限制，并提升进程优先级避免被 HANS 冻结
        val notification = buildNotification(alarmId, remark, ringtoneUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 2. 异步处理数据库状态更新与铃声播放，避免阻塞主线程
        serviceScope.launch {
            if (alarmId != null && alarmId != "temp_snooze") {
                launch(Dispatchers.IO) {
                    try {
                        val alarm = alarmDao.getById(alarmId)
                        if (alarm != null) {
                            if (alarm.repeatDays == 0) {
                                // 单次闹钟：响铃后禁用
                                alarmDao.setEnabled(alarmId, false)
                            } else {
                                // 重复闹钟：调度下一次触发
                                AlarmScheduler.scheduleAlarm(this@AlarmService, alarm)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reschedule or update alarm state: $alarmId", e)
                    }
                }
            }

            playAlarmRingtoneAsync(ringtoneUri)
        }

        return START_STICKY
    }

    private suspend fun playAlarmRingtoneAsync(customUri: String?) {
        val source = try {
            configRepo.ringtoneSource.first()
        } catch (e: Exception) {
            "local"
        }

        if (source == "built_in") {
            val fileName = try {
                configRepo.builtInRingtone.first()
            } catch (e: Exception) {
                "alert.mp3"
            }
            try {
                withContext(Dispatchers.IO) {
                    val mp = MediaPlayer().apply {
                        val assetFileDescriptor = assets.openFd(fileName)
                        setDataSource(
                            assetFileDescriptor.fileDescriptor,
                            assetFileDescriptor.startOffset,
                            assetFileDescriptor.length
                        )
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = true
                        prepare()
                    }
                    mediaPlayer = mp
                    mp.start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed playing built-in ringtone: $fileName, fallback to default", e)
                playLocalRingtoneAsync(customUri)
            }
        } else {
            playLocalRingtoneAsync(customUri)
        }
    }

    private suspend fun playLocalRingtoneAsync(customUri: String?) {
        val actualUriString = if (!customUri.isNullOrBlank()) {
            if (customUri.contains("|")) {
                customUri.split("|")[0]
            } else {
                customUri
            }
        } else {
            null
        }

        val alarmUri: Uri = if (!actualUriString.isNullOrBlank()) {
            Uri.parse(actualUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            withContext(Dispatchers.IO) {
                val mp = MediaPlayer().apply {
                    if (alarmUri.scheme == "file") {
                        val path = alarmUri.path
                        if (!path.isNullOrBlank()) {
                            setDataSource(path)
                        } else {
                            setDataSource(this@AlarmService, alarmUri)
                        }
                    } else {
                        setDataSource(this@AlarmService, alarmUri)
                    }
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                }
                mediaPlayer = mp
                mp.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing local ringtone: $customUri, fallback to system alarm", e)
            if (!customUri.isNullOrBlank()) {
                playLocalRingtoneAsync(null)
            }
        }
    }

    private fun buildNotification(alarmId: String?, remark: String, ringtoneUri: String?): Notification {
        val fullScreenIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("isIndependentAlarm", true)
            putExtra("alarmRemark", remark)
            putExtra("alarmId", alarmId)
            ringtoneUri?.let { putExtra("ringtoneUri", it) }
        }

        val pending = PendingIntent.getActivity(
            this,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Action button to stop alarm
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPending = PendingIntent.getService(
            this,
            2002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("独立闹钟")
            .setContentText(remark)
            .setFullScreenIntent(pending, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(R.drawable.ic_notification, "关闭", stopPending)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "独立闹钟后台服务",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于保证后台闹钟正常响铃"
                setBypassDnd(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        serviceScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
