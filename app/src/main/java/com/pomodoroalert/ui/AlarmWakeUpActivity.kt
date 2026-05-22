package com.pomodoroalert.ui

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pomodoroalert.MainActivity
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.service.TimerService
import com.pomodoroalert.service.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmWakeUpActivity : ComponentActivity() {

    @Inject lateinit var taskRepo: TaskRepository
    
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val taskId = intent.getStringExtra("taskId")
        
        val customRingtoneUri = intent.getStringExtra("ringtoneUri")
        playAlarmRingtone(customRingtoneUri)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.8f) // 半透明黑色背景
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isAlarm = intent.getBooleanExtra("isIndependentAlarm", false)
                        val titleText = if (isAlarm) "闹钟提醒" else "时间到，请休息"
                        val customRemark = intent.getStringExtra("alarmRemark")
                        
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                        if (isAlarm && !customRemark.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = customRemark,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(64.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = {
                                handleComplete(taskId)
                            }) {
                                Text("完成")
                            }
                            Button(onClick = {
                                handlePostpone(taskId)
                            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                Text("推迟 10 分钟")
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun playAlarmRingtone(customUri: String?) {
        val actualUriString = if (!customUri.isNullOrBlank()) {
            if (customUri.contains("|")) {
                customUri.split("|")[0]
            } else {
                customUri
            }
        } else {
            null
        }

        val alarmUri: Uri = if (!actualUriString.isNullOrBlank()) {
            Uri.parse(actualUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                if (alarmUri.scheme == "file") {
                    val path = alarmUri.path
                    if (!path.isNullOrBlank()) {
                        setDataSource(path)
                    } else {
                        setDataSource(this@AlarmWakeUpActivity, alarmUri)
                    }
                } else {
                    setDataSource(this@AlarmWakeUpActivity, alarmUri)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果自定义铃声播放失败，回退到默认铃声
            if (!customUri.isNullOrBlank()) {
                playAlarmRingtone(null)
            }
        }
    }
    
    private fun stopAlarm() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    private fun handleComplete(taskId: String?) {
        stopAlarm()
        val isAlarm = intent.getBooleanExtra("isIndependentAlarm", false)
        
        if (isAlarm) {
            // 独立闹钟：仅关闭并返回
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (taskId != null) {
                taskRepo.updateStatus(taskId, "已完成")
            }
            stopService(Intent(this@AlarmWakeUpActivity, TimerService::class.java))
            TimerState.remainingTime.value = 0L
            val mainIntent = Intent(this@AlarmWakeUpActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(mainIntent)
            finish()
        }
    }

    private fun handlePostpone(taskId: String?) {
        stopAlarm()
        val isAlarm = intent.getBooleanExtra("isIndependentAlarm", false)
        
        if (isAlarm) {
            // 独立闹钟：重新调度 10 分钟后的闹钟
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MINUTE, 10)
            }
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(this, com.pomodoroalert.receiver.IndependentAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
            )
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = Intent(this, com.pomodoroalert.MainActivity::class.java)
                val showPi = android.app.PendingIntent.getActivity(
                    this,
                    1001,
                    showIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0
                )
                val info = android.app.AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPi)
                alarmManager.setAlarmClock(info, pendingIntent)
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            }
            android.widget.Toast.makeText(this, "闹钟已推迟 10 分钟", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 番茄钟逻辑：增加 10 分钟并重启服务
        val newDuration = 10 * 60_000L
        val timerIntent = Intent(this, TimerService::class.java).apply {
            putExtra("duration", newDuration)
            putExtra("taskId", taskId)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(timerIntent)
        } else {
            startService(timerIntent)
        }
        finish()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
