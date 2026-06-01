package com.pomodoroalert.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.pomodoroalert.MainActivity
import com.pomodoroalert.data.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    private fun getPendingIntent(context: Context, alarm: AlarmEntity, flags: Int): PendingIntent? {
        val intent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarmId", alarm.alarmId)
            putExtra("alarmRemark", alarm.remark.ifBlank { "闹钟时间到了！" })
            alarm.ringtoneUri?.let { putExtra("ringtoneUri", it) }
        }
        val requestCode = alarm.alarmId.hashCode() and 0x7FFFFFFF

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, requestCode, intent, flags)
        } else {
            PendingIntent.getService(context, requestCode, intent, flags)
        }
    }

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = getPendingIntent(
            context, alarm,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        ) ?: return

        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = Intent(context, MainActivity::class.java)
                val showPi = PendingIntent.getActivity(
                    context,
                    (alarm.alarmId.hashCode() and 0x7FFFFFFF) + 1,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
                am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, showPi), pi)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                }
            }
            Log.d(TAG, "Successfully scheduled alarm clock for ${alarm.hour}:${alarm.minute}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "缺少精确闹钟权限，可能导致提醒不准时", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelAlarm(context: Context, alarm: AlarmEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = getPendingIntent(
            context, alarm,
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (pi != null) {
            am.cancel(pi)
            pi.cancel()
            Log.d(TAG, "Cancelled scheduled alarm ${alarm.alarmId}")
        }
    }
}
