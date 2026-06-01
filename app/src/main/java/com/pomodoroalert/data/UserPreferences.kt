package com.pomodoroalert.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_EARPHONE_MODE = booleanPreferencesKey("earphone_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_RINGTONE_SOURCE = stringPreferencesKey("ringtone_source")
        val KEY_BUILT_IN_RINGTONE = stringPreferencesKey("built_in_ringtone")
    }

    val earphoneMode = context.dataStore.data.map { it[KEY_EARPHONE_MODE] ?: true }
    val language = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "zh" }
    val ringtoneSource = context.dataStore.data.map { it[KEY_RINGTONE_SOURCE] ?: "local" }
    val builtInRingtone = context.dataStore.data.map { it[KEY_BUILT_IN_RINGTONE] ?: "alert.mp3" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setEarphoneMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_EARPHONE_MODE] = enabled }
    }

    suspend fun setRingtoneSource(source: String) {
        context.dataStore.edit { it[KEY_RINGTONE_SOURCE] = source }
    }

    suspend fun setBuiltInRingtone(ringtone: String) {
        context.dataStore.edit { it[KEY_BUILT_IN_RINGTONE] = ringtone }
    }
}
