package com.pomodoroalert.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ConfigRepository(private val userPrefs: UserPreferences) {
    val earphoneMode: Flow<Boolean> = userPrefs.earphoneMode
    val defaultPomodoro: Flow<Int> = userPrefs.defaultPomodoro
    val language: Flow<String> = userPrefs.language
    val ringtoneSource: Flow<String> = userPrefs.ringtoneSource
    val builtInRingtone: Flow<String> = userPrefs.builtInRingtone

    suspend fun setEarphoneMode(enabled: Boolean) = userPrefs.setEarphoneMode(enabled)
    suspend fun setDefaultPomodoro(minutes: Int) = userPrefs.setDefaultPomodoro(minutes)
    suspend fun setLanguage(lang: String) = userPrefs.setLanguage(lang)
    suspend fun setRingtoneSource(source: String) = userPrefs.setRingtoneSource(source)
    suspend fun setBuiltInRingtone(ringtone: String) = userPrefs.setBuiltInRingtone(ringtone)

    suspend fun getDefaultPomodoro(): Int = userPrefs.defaultPomodoro.first()
}
