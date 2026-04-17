package com.pomodoroalert.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import androidx.core.content.getSystemService
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    init {
        tts = TextToSpeech(context, this)
        audioManager = context.getSystemService()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    /** Request transient audio focus with ducking */
    fun requestFocusDuck() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attrs)
            .setWillPauseWhenDucked(true)
            .build()
        audioManager?.requestAudioFocus(focusRequest!!)
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
    }

    /** Speak text via TTS using alarm stream */
    fun speak(text: String) {
        requestFocusDuck()
        tts?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PomodoroAlertTTS")
        // Release after speaking (approx 3s)
        tts?.setOnUtteranceProgressListener(object : TextToSpeech.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { abandonFocus() }
            override fun onError(utteranceId: String?) { abandonFocus() }
        })
    }
}
