package com.pomodoroalert.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pomodoroalert.R
import com.pomodoroalert.ui.AlarmWakeUpActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IndependentAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarmId")
        if (alarmId != null) {
            val pendingResult = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val db = androidx.room.Room.databaseBuilder(
                        context.applicationContext,
                        com.pomodoroalert.data.AppDatabase::class.java,
                        "pomodoro_db"
                    ).fallbackToDestructiveMigration().build()
                    val dao = db.alarmDao()
                    val alarm = dao.getById(alarmId)
                    if (alarm != null && alarm.repeatDays == 0) {
                        dao.setEnabled(alarmId, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }

        WakeLockManager.acquire(context)
        
        val channelId = "independent_alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "独立闹钟提醒"
            val channel = android.app.NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                description = "用于准时提醒闹钟"
                setBypassDnd(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        // 优先从 intent extra 读取（多闹钟模式），回退到旧的 SharedPreferences
        val remark = intent.getStringExtra("alarmRemark")
            ?: context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                .getString("alarm_remark", "闹钟时间到了！")
            ?: "闹钟时间到了！"
        
        val ringtoneUri = intent.getStringExtra("ringtoneUri")
        
        val fullScreenIntent = Intent(context, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("isIndependentAlarm", true)
            putExtra("alarmRemark", remark)
            ringtoneUri?.let { putExtra("ringtoneUri", it) }
        }
        val pending = PendingIntent.getActivity(
            context,
            2001,
            fullScreenIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("独立闹钟")
            .setContentText(remark)
            .setFullScreenIntent(pending, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
            
        try {
            NotificationManagerCompat.from(context).notify(3001, notification)
            context.startActivity(fullScreenIntent) // Try to start directly if in foreground
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        WakeLockManager.releaseDelayed(context, 5000L)
    }
}
