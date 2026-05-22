package com.pomodoroalert.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

private const val TAG = "PomodoroTTS"

class VoiceManager(private val context: Context) {
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.d(TAG, "VoiceManager init (MediaPlayer mode)")
    }

    private fun requestFocus() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attrs)
            .build()
        val result = audioManager?.requestAudioFocus(focusRequest!!)
        Log.d(TAG, "requestAudioFocus result=$result")
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /** Play the alert WAV from assets */
    fun speak(text: String) {
        Log.d(TAG, "speak() called, text=$text")
        requestFocus()
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                val afd = context.assets.openFd("alert.mp3")
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer onCompletion")
                    abandonFocus()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                    abandonFocus()
                    false
                }
                start()
                Log.d(TAG, "MediaPlayer started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed: ${e.message}", e)
            abandonFocus()
        }
    }
}
