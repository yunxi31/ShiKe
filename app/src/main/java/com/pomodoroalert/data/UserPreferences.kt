package com.pomodoroalert.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_EARPHONE_MODE = booleanPreferencesKey("earphone_mode")
        val KEY_DEFAULT_POMODORO = intPreferencesKey("default_pomodoro")
        val KEY_VOICE_TONE = stringPreferencesKey("voice_tone")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    val earphoneMode = context.dataStore.data.map { it[KEY_EARPHONE_MODE] ?: true }
    val defaultPomodoro = context.dataStore.data.map { it[KEY_DEFAULT_POMODORO] ?: 25 }
    val voiceTone = context.dataStore.data.map { it[KEY_VOICE_TONE] ?: "default" }
    val language = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "zh" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setEarphoneMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_EARPHONE_MODE] = enabled }
    }
    suspend fun setDefaultPomodoro(minutes: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_POMODORO] = minutes }
    }
    suspend fun setVoiceTone(tone: String) {
        context.dataStore.edit { it[KEY_VOICE_TONE] = tone }
    }
}
