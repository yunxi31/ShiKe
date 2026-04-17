package com.pomodoroalert.receiver

import android.content.Context
import android.os.PowerManager
import android.os.Handler
import android.os.Looper

object WakeLockManager {
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())

    fun acquire(context: Context) {
        if (wakeLock?.isHeld == true) return
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PomodoroAlert::WakeLock").apply {
            acquire(10_000L) // 10 seconds max
        }
    }

    fun release(context: Context) {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    fun releaseDelayed(context: Context, delayMs: Long) {
        handler.postDelayed({ release(context) }, delayMs)
    }
}
