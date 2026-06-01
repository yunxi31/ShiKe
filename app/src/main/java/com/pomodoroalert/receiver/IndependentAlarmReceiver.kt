package com.pomodoroalert.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
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
import java.util.Calendar

class IndependentAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = androidx.room.Room.databaseBuilder(
                        context.applicationContext,
                        com.pomodoroalert.data.AppDatabase::class.java,
                        "pomodoro_db"
                    ).fallbackToDestructiveMigration().build()
                    db.alarmDao().getAllAlarmsOnce().filter { it.isEnabled }.forEach { alarm ->
                        com.pomodoroalert.service.AlarmScheduler.scheduleAlarm(context, alarm)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
