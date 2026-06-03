package com.pomodoroalert.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.AlarmEntity
import com.pomodoroalert.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ScheduleDraft(
    val id: String = UUID.randomUUID().toString(),
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val content: String,
    val voiceMode: String = "TTS",
    val voiceText: String = "",
    val audioUri: String? = null,
    val audioFileName: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val alarmDao: AlarmDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)

    private val _markdownText = MutableStateFlow("")
    val markdownText: StateFlow<String> = _markdownText.asStateFlow()

    private val _drafts = MutableStateFlow<List<ScheduleDraft>>(emptyList())
    val drafts: StateFlow<List<ScheduleDraft>> = _drafts.asStateFlow()

    private val _savedAlarms = MutableStateFlow<List<AlarmEntity>>(emptyList())
    val savedAlarms: StateFlow<List<AlarmEntity>> = _savedAlarms.asStateFlow()

    init {
        // Load raw markdown
        _markdownText.value = sharedPrefs.getString("markdown_text", "") ?: ""
        
        // Load active alarms from room
        loadSavedAlarms()
    }

    fun loadSavedAlarms() {
        viewModelScope.launch {
            val alarms = alarmDao.getScheduleAlarmsOnce()
            _savedAlarms.value = alarms
            
            // If drafts is empty and we have saved alarms, populate drafts from saved markdown
            if (_drafts.value.isEmpty() && _markdownText.value.isNotBlank()) {
                parseMarkdown(_markdownText.value, useSavedCustomizations = true)
            }
        }
    }

    private var autoSaveJob: kotlinx.coroutines.Job? = null

    fun updateMarkdownText(text: String) {
        _markdownText.value = text
        parseMarkdown(text)
        
        // Trigger auto-save debounce
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            autoSaveScheduleInBackground()
        }
    }

    private suspend fun autoSaveScheduleInBackground() {
        val currentDrafts = _drafts.value
        val isBlank = _markdownText.value.isBlank()
        if (currentDrafts.isEmpty() && !isBlank) {
            // Do not auto-save invalid text as empty alarms to prevent wiping out existing alarms while typing
            return
        }

        try {
            // 1. Get and cancel existing schedule alarms, and delete their copied ringtone files
            val existingAlarms = alarmDao.getScheduleAlarmsOnce()
            for (alarm in existingAlarms) {
                AlarmScheduler.cancelAlarm(context, alarm)
                // Delete internal audio file if any
                alarm.audioUri?.let { uriStr ->
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

            // 2. Delete existing schedule alarms in DB
            alarmDao.deleteScheduleAlarms()

            // 3. Convert drafts to AlarmEntity and save + schedule
            for (draft in currentDrafts) {
                val alarm = AlarmEntity(
                    alarmId = draft.id,
                    hour = draft.startHour,
                    minute = draft.startMinute,
                    remark = draft.content,
                    isEnabled = true,
                    repeatDays = 127, // Repeat every day
                    alarmType = "SCHEDULE",
                    voiceMode = draft.voiceMode,
                    voiceText = draft.voiceText,
                    audioUri = draft.audioUri
                )
                alarmDao.insert(alarm)
                AlarmScheduler.scheduleAlarm(context, alarm)
            }

            // 4. Save raw markdown text to SharedPreferences
            sharedPrefs.edit().putString("markdown_text", _markdownText.value).apply()

            // 5. Reload saved alarms
            val alarms = alarmDao.getScheduleAlarmsOnce()
            _savedAlarms.value = alarms
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerImmediateAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            autoSaveScheduleInBackground()
        }
    }

    fun parseMarkdown(text: String, useSavedCustomizations: Boolean = false) {
        val lines = text.lines()
        // Regex format: 06:00 - 06:40 起床洗漱、打八部金刚功
        val regex = Regex("""([0-9]{1,2})[:：]([0-9]{2})\s*[-—~～]\s*([0-9]{1,2})[:：]([0-9]{2})\s*(.*)""")
        
        val newDrafts = mutableListOf<ScheduleDraft>()
        val currentDrafts = _drafts.value
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            val match = regex.find(trimmed)
            if (match != null) {
                val startHour = match.groupValues[1].toInt()
                val startMinute = match.groupValues[2].toInt()
                val endHour = match.groupValues[3].toInt()
                val endMinute = match.groupValues[4].toInt()
                val content = match.groupValues[5].trim()
                
                // Set default voice text
                val defaultVoiceText = "主人，该${content}了，今天又是崭新的一天"
                
                // 1. Try to find in current drafts in memory to preserve unsaved user modifications
                val currentMatch = currentDrafts.find { it.startHour == startHour && it.startMinute == startMinute }
                
                var voiceMode = currentMatch?.voiceMode ?: "TTS"
                var voiceText = currentMatch?.voiceText ?: defaultVoiceText
                var audioUri = currentMatch?.audioUri
                var audioFileName = currentMatch?.audioFileName
                
                // 2. If not found in memory drafts and useSavedCustomizations is true, try to find in database saved alarms
                if (currentMatch == null && useSavedCustomizations) {
                    val existing = _savedAlarms.value.find { it.hour == startHour && it.minute == startMinute }
                    if (existing != null) {
                        voiceMode = existing.voiceMode
                        voiceText = existing.voiceText.ifBlank { defaultVoiceText }
                        audioUri = existing.audioUri
                        if (audioUri != null && audioUri.contains("|")) {
                            audioFileName = audioUri.split("|").getOrNull(1)
                        }
                    }
                }
                
                newDrafts.add(
                    ScheduleDraft(
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        content = content,
                        voiceMode = voiceMode,
                        voiceText = voiceText,
                        audioUri = audioUri,
                        audioFileName = audioFileName
                    )
                )
            }
        }
        _drafts.value = newDrafts
    }

    fun updateDraftVoiceMode(draftId: String, voiceMode: String) {
        _drafts.value = _drafts.value.map {
            if (it.id == draftId) {
                it.copy(voiceMode = voiceMode)
            } else {
                it
            }
        }
        triggerImmediateAutoSave()
    }

    fun updateDraftVoiceText(draftId: String, voiceText: String) {
        _drafts.value = _drafts.value.map {
            if (it.id == draftId) {
                it.copy(voiceText = voiceText)
            } else {
                it
            }
        }
        triggerImmediateAutoSave()
    }

    fun updateDraftAudio(draftId: String, uriStr: String?, fileName: String?) {
        _drafts.value = _drafts.value.map {
            if (it.id == draftId) {
                it.copy(audioUri = uriStr, audioFileName = fileName)
            } else {
                it
            }
        }
        triggerImmediateAutoSave()
    }

    fun saveAndApplySchedule(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentDrafts = _drafts.value
        val isBlank = _markdownText.value.isBlank()
        if (currentDrafts.isEmpty() && !isBlank) {
            onError("没有匹配到任何有效的作息安排，请检查时间格式（如 06:00 - 06:40 起床）")
            return
        }

        autoSaveJob?.cancel() // Cancel any pending background auto-save to run immediately and synchronously
        viewModelScope.launch {
            try {
                // 1. Get and cancel existing schedule alarms, and delete their copied ringtone files
                val existingAlarms = alarmDao.getScheduleAlarmsOnce()
                for (alarm in existingAlarms) {
                    AlarmScheduler.cancelAlarm(context, alarm)
                    // Delete internal audio file if any
                    alarm.audioUri?.let { uriStr ->
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

                // 2. Delete existing schedule alarms in DB
                alarmDao.deleteScheduleAlarms()

                // 3. Convert drafts to AlarmEntity and save + schedule
                for (draft in currentDrafts) {
                    val alarm = AlarmEntity(
                        alarmId = draft.id, // Reuse draft id to match the copied custom ringtone filename
                        hour = draft.startHour,
                        minute = draft.startMinute,
                        remark = draft.content,
                        isEnabled = true,
                        repeatDays = 127, // Repeat every day of week (Monday to Sunday)
                        alarmType = "SCHEDULE",
                        voiceMode = draft.voiceMode,
                        voiceText = draft.voiceText,
                        audioUri = draft.audioUri
                    )
                    alarmDao.insert(alarm)
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }

                // 4. Save raw markdown text to SharedPreferences
                sharedPrefs.edit().putString("markdown_text", _markdownText.value).apply()

                // 5. Reload saved alarms
                loadSavedAlarms()

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("保存失败: ${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val savedMarkdown = sharedPrefs.getString("markdown_text", "") ?: ""
        if (_markdownText.value.trim() != savedMarkdown.trim()) {
            return true
        }

        val currentDrafts = _drafts.value
        val savedAlarmsList = _savedAlarms.value

        if (currentDrafts.size != savedAlarmsList.size) {
            return currentDrafts.isNotEmpty() || savedAlarmsList.isNotEmpty()
        }

        for (draft in currentDrafts) {
            val matchingAlarm = savedAlarmsList.find { it.hour == draft.startHour && it.minute == draft.startMinute }
            if (matchingAlarm == null) return true
            if (matchingAlarm.voiceMode != draft.voiceMode) return true
            if (matchingAlarm.voiceText != draft.voiceText) return true
            if (matchingAlarm.audioUri != draft.audioUri) return true
            if (matchingAlarm.remark != draft.content) return true
        }

        return false
    }
}
