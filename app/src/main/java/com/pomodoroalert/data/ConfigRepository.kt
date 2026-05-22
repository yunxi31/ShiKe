package com.pomodoroalert.data

import com.pomodoroalert.data.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ConfigRepository(private val userPrefs: UserPreferences) {
    val earphoneMode: Flow<Boolean> = userPrefs.earphoneMode
    val defaultPomodoro: Flow<Int> = userPrefs.defaultPomodoro
    val voiceTone: Flow<String> = userPrefs.voiceTone
    val language: Flow<String> = userPrefs.language

    suspend fun setEarphoneMode(enabled: Boolean) = userPrefs.setEarphoneMode(enabled)
    suspend fun setDefaultPomodoro(minutes: Int) = userPrefs.setDefaultPomodoro(minutes)
    suspend fun setVoiceTone(tone: String) = userPrefs.setVoiceTone(tone)
    suspend fun setLanguage(lang: String) = userPrefs.setLanguage(lang)

    // Helper for ViewModel to get current value (suspend)
    suspend fun getDefaultPomodoro(): Int = userPrefs.defaultPomodoro.first()
}
