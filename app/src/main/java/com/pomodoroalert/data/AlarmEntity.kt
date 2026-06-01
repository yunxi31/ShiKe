package com.pomodoroalert.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val alarmId: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val remark: String = "",
    val isEnabled: Boolean = true,
    /** 重复日 bitmask: bit0=周一 … bit6=周日, 0=仅一次 */
    val repeatDays: Int = 0,
    /** 自定义铃声 URI，null 表示使用系统默认闹钟铃声 */
    val ringtoneUri: String? = null,
    /** 触发时是否弹出锁屏全屏界面 */
    val lockScreenEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
