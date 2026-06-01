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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodoroalert.MainActivity
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.data.ConfigRepository
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.AlarmEntity
import com.pomodoroalert.service.TimerService
import com.pomodoroalert.service.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AlarmWakeUpActivity : ComponentActivity() {

    @Inject lateinit var taskRepo: TaskRepository
    @Inject lateinit var configRepo: ConfigRepository
    @Inject lateinit var alarmDao: AlarmDao
    
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. 调用系统 API (Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        // 2. 备用并结合窗口 Flag，以兼容各定制系统 (如 ColorOS, MIUI 等)
        @Suppress("DEPRECATION")
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        val taskId = intent.getStringExtra("taskId")
        val isAlarm = intent.getBooleanExtra("isIndependentAlarm", false)
        
        if (!isAlarm) {
            val customRingtoneUri = intent.getStringExtra("ringtoneUri")
            playAlarmRingtone(customRingtoneUri)
        }

        setContent {
            MaterialTheme {
                val quoteState = configRepo.motivationalQuote.collectAsState(initial = "今天又是充满希望的一天，加油！")
                val customRemark = intent.getStringExtra("alarmRemark") ?: ""

                val currentTime = remember { mutableStateOf("") }
                val currentDate = remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    while (true) {
                        val calendar = java.util.Calendar.getInstance()
                        currentTime.value = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
                        kotlinx.coroutines.delay(1000)
                    }
                }

                LaunchedEffect(Unit) {
                    val sdf = java.text.SimpleDateFormat("M月d日 EEEE", java.util.Locale.CHINA)
                    currentDate.value = sdf.format(java.util.Date())
                }

                val backgroundGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F101A), Color(0xFF1B1D30))
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundGradient)
                            .padding(horizontal = 24.dp, vertical = 56.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Clock Area
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 40.dp)
                            ) {
                                Text(
                                    text = currentTime.value,
                                    fontSize = 76.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentDate.value,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Middle Suggestion Card
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "“",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6C5DD3),
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = quoteState.value,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 26.sp
                                        )
                                        if (customRemark.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "备注: $customRemark",
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom Actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { handlePostpone(taskId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("推迟 10 分钟", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { handleComplete(taskId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF5252),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("关闭闹钟", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun playAlarmRingtone(customUri: String?) {
        val source = try {
            runBlocking { configRepo.ringtoneSource.first() }
        } catch (e: Exception) {
            "local"
        }

        if (source == "built_in") {
            val fileName = try {
                runBlocking { configRepo.builtInRingtone.first() }
            } catch (e: Exception) {
                "alert.mp3"
            }
            try {
                mediaPlayer = MediaPlayer().apply {
                    val assetFileDescriptor = assets.openFd(fileName)
                    setDataSource(
                        assetFileDescriptor.fileDescriptor,
                        assetFileDescriptor.startOffset,
                        assetFileDescriptor.length
                    )
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
                playLocalRingtone(customUri)
            }
        } else {
            playLocalRingtone(customUri)
        }
    }

    private fun playLocalRingtone(customUri: String?) {
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
                playLocalRingtone(null)
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
            // 独立闹钟：停止响铃服务并退出
            stopService(Intent(this, com.pomodoroalert.service.AlarmService::class.java))
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
            stopService(Intent(this, com.pomodoroalert.service.AlarmService::class.java))
            val alarmId = intent.getStringExtra("alarmId")
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MINUTE, 10)
            }
            val snoozeTimeMillis = calendar.timeInMillis
            val newHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val newMinute = calendar.get(java.util.Calendar.MINUTE)

            if (alarmId != null) {
                // 有具体 ID 的闹钟：更新数据库里的闹钟时间，确保列表页面显示的时间更新且处于启用状态
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val alarm = alarmDao.getById(alarmId)
                        if (alarm != null) {
                            val updatedAlarm = alarm.copy(
                                hour = newHour,
                                minute = newMinute,
                                isEnabled = true
                            )
                            alarmDao.update(updatedAlarm)

                            // 重新调度系统的 AlarmManager 闹钟
                            com.pomodoroalert.service.AlarmScheduler.scheduleAlarm(this@AlarmWakeUpActivity, updatedAlarm)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // 兜底逻辑：如果没有 alarmId，注册一个临时闹钟进行提醒
                val tempAlarm = com.pomodoroalert.data.AlarmEntity(
                    alarmId = "temp_snooze",
                    hour = newHour,
                    minute = newMinute,
                    remark = "闹钟推迟提醒"
                )
                com.pomodoroalert.service.AlarmScheduler.scheduleAlarm(this, tempAlarm)
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
        val isAlarm = intent.getBooleanExtra("isIndependentAlarm", false)
        if (isAlarm) {
            stopService(Intent(this, com.pomodoroalert.service.AlarmService::class.java))
        }
        super.onDestroy()
    }
}
