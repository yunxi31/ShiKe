package com.pomodoroalert

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PomodoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Independent Alarm Channel (v2) - High Importance
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alarmChannel = NotificationChannel(
                "alarm_service_channel_v2",
                "独立闹钟后台服务",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于保证后台闹钟正常响铃"
                setBypassDnd(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(alarmChannel)

            // 2. Timer Service Channel - Low Importance
            val timerChannel = NotificationChannel(
                "timer_service_channel",
                "番茄钟前台服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "番茄钟运行状态展示通知"
            }
            manager.createNotificationChannel(timerChannel)
        }
    }
}
