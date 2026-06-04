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
import com.pomodoroalert.data.ConfigRepository
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
    private val configRepo: ConfigRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ringtoneSource: StateFlow<String> = configRepo.ringtoneSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "local")

    val builtInRingtone: StateFlow<String> = configRepo.builtInRingtone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "alert.mp3")

    fun addAlarm(hour: Int, minute: Int, remark: String, ringtoneUri: String? = null, lockScreenEnabled: Boolean = true) {
        viewModelScope.launch {
            val alarmId = java.util.UUID.randomUUID().toString()
            val alarm = AlarmEntity(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                remark = remark,
                ringtoneUri = ringtoneUri,
                lockScreenEnabled = lockScreenEnabled
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

    suspend fun getAlarmById(alarmId: String): AlarmEntity? {
        return alarmDao.getById(alarmId)
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
        com.pomodoroalert.service.AlarmScheduler.scheduleAlarm(context, alarm)
    }

    private fun cancelSystemAlarm(alarm: AlarmEntity) {
        com.pomodoroalert.service.AlarmScheduler.cancelAlarm(context, alarm)
    }
}
