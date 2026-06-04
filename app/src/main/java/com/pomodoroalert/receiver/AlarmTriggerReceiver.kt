package com.pomodoroalert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.pomodoroalert.service.AlarmService

/**
 * AlarmTriggerReceiver — 闹钟触发中转 Receiver
 *
 * AlarmManager 直接启动 ForegroundService 在 OEM 设备（ColorOS/MIUI 等）上
 * 因后台限制而无法触发。正确链路是：
 *   AlarmManager → BroadcastReceiver (this) → AlarmService
 *
 * Receiver 在主线程被立即唤起（即使设备休眠），然后我们从这里启动 AlarmService。
 */
class AlarmTriggerReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmTriggerReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: alarm triggered, alarmId=${intent.getStringExtra("alarmId")}")

        // 1. 立即获取 WakeLock，防止设备在我们启动服务前重新休眠
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wl = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "PomodoroAlert::AlarmTriggerWakeLock"
            )
            wl.acquire(15_000L) // 15 秒足够 AlarmService 完成前台启动
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }

        // 2. 把所有 extras 原样转发给 AlarmService
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            intent.extras?.let { putExtras(it) }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "AlarmService started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AlarmService", e)
        }
    }
}
