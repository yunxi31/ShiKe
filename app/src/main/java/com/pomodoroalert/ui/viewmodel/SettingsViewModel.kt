package com.pomodoroalert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoroalert.data.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepo: ConfigRepository
) : ViewModel() {
    val earphoneMode: StateFlow<Boolean> = configRepo.earphoneMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultPomodoro: StateFlow<Int> = configRepo.defaultPomodoro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val language: StateFlow<String> = configRepo.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh")

    val ringtoneSource: StateFlow<String> = configRepo.ringtoneSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "local")

    val builtInRingtone: StateFlow<String> = configRepo.builtInRingtone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "alert.mp3")

    fun setEarphoneMode(enabled: Boolean) {
        viewModelScope.launch { configRepo.setEarphoneMode(enabled) }
    }

    fun setDefaultPomodoro(minutes: Int) {
        viewModelScope.launch { configRepo.setDefaultPomodoro(minutes) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { configRepo.setLanguage(lang) }
    }

    fun setRingtoneSource(source: String) {
        viewModelScope.launch { configRepo.setRingtoneSource(source) }
    }

    fun setBuiltInRingtone(ringtone: String) {
        viewModelScope.launch { configRepo.setBuiltInRingtone(ringtone) }
    }
}
