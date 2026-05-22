package com.pomodoroalert.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.AlarmEntity
import com.pomodoroalert.receiver.IndependentAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val alarmDao: AlarmDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hour: Int, minute: Int, remark: String, ringtoneUri: String? = null) {
        viewModelScope.launch {
            val alarmId = java.util.UUID.randomUUID().toString()
            val alarm = AlarmEntity(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                remark = remark,
                ringtoneUri = ringtoneUri
            )
            alarmDao.insert(alarm)
            scheduleSystemAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.update(alarm)
            if (alarm.isEnabled) {
                scheduleSystemAlarm(alarm)
            } else {
                cancelSystemAlarm(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            cancelSystemAlarm(alarm)
            alarmDao.delete(alarm)
            alarm.ringtoneUri?.let { uriStr ->
                if (uriStr.contains("|")) {
                    val fileUriStr = uriStr.split("|")[0]
                    try {
                        val uri = Uri.parse(fileUriStr)
                        if (uri.scheme == "file") {
                            val path = uri.path
                            if (path != null) {
                                val file = java.io.File(path)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val toggled = !alarm.isEnabled
            alarmDao.setEnabled(alarm.alarmId, toggled)
            if (toggled) {
                scheduleSystemAlarm(alarm.copy(isEnabled = true))
            } else {
                cancelSystemAlarm(alarm)
            }
        }
    }

    // ── System alarm scheduling ────────────────────────────────

    private fun requestCode(alarm: AlarmEntity): Int =
        alarm.alarmId.hashCode() and 0x7FFFFFFF

    private fun scheduleSystemAlarm(alarm: AlarmEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, IndependentAlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.alarmId)
            putExtra("alarmRemark", alarm.remark.ifBlank { "闹钟时间到了！" })
            alarm.ringtoneUri?.let { putExtra("ringtoneUri", it) }
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode(alarm), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

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
                val showIntent = Intent(context, com.pomodoroalert.MainActivity::class.java)
                val showPi = PendingIntent.getActivity(
                    context,
                    requestCode(alarm),
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
                val info = AlarmManager.AlarmClockInfo(cal.timeInMillis, showPi)
                am.setAlarmClock(info, pi)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "缺少精确闹钟权限，可能导致提醒不准时", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelSystemAlarm(alarm: AlarmEntity) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, IndependentAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode(alarm), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        am.cancel(pi)
    }
}
