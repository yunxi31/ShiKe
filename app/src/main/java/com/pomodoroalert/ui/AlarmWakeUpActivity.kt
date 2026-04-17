package com.pomodoroalert.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pomodoroalert.R
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.service.TimerService
import com.pomodoroalert.voice.VoiceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmWakeUpActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager
    @Inject lateinit var taskRepo: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        
        voiceManager = VoiceManager(this)
        voiceManager.speak("时间到，请休息，完成得很棒！")

        val taskId = intent.getStringExtra("taskId")

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
                        Text(
                            text = "时间到，请休息",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
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

    private fun handleComplete(taskId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (taskId != null) {
                taskRepo.updateStatus(taskId, "已完成")
            }
            stopService(Intent(this@AlarmWakeUpActivity, TimerService::class.java))
            finish()
        }
    }

    private fun handlePostpone(taskId: String?) {
        // Simple postpone by adding 10 mins back to the exact alarm
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
        super.onDestroy()
        voiceManager.abandonFocus()
    }
}
