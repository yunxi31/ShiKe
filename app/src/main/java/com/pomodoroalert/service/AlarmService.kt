package com.pomodoroalert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pomodoroalert.MainActivity
import com.pomodoroalert.R
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.ConfigRepository
import com.pomodoroalert.ui.AlarmWakeUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        private const val TAG = "AlarmService"
        private const val CHANNEL_ID = "alarm_service_channel_v2"
        private const val NOTIFICATION_ID = 3001
    }

    @Inject lateinit var configRepo: ConfigRepository
    @Inject lateinit var alarmDao: AlarmDao

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopAndReleaseResources()
            stopSelf()
            return START_NOT_STICKY
        }

        stopAndReleaseResources()

        val alarmId = intent?.getStringExtra("alarmId")
        val remark = intent?.getStringExtra("alarmRemark") ?: "闹钟时间到了！"
        val ringtoneUri = intent?.getStringExtra("ringtoneUri")
        val alarmType = intent?.getStringExtra("alarmType") ?: "REGULAR"
        val voiceMode = intent?.getStringExtra("voiceMode") ?: "NONE"
        val voiceText = intent?.getStringExtra("voiceText") ?: ""
        val audioUri = intent?.getStringExtra("audioUri")

        // 1. 立即在前台启动一个静音低优先级的常驻服务通知，以满足后台限制
        val serviceNotification = NotificationCompat.Builder(this, "timer_service_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("闹钟服务")
            .setContentText("闹钟后台运行中")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, serviceNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, serviceNotification)
        }

        val lockScreenEnabled = intent?.getBooleanExtra("lockScreenEnabled", true) ?: true
        // 2. 发送独立的高优先级闹钟提示通知（非 ongoing 状态，可触发全屏 Intent 弹窗）
        val alarmNotification = buildNotification(alarmId, remark, ringtoneUri, alarmType, lockScreenEnabled)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(3002, alarmNotification)

        if (lockScreenEnabled) {
            // A. Acquire WakeLock with SCREEN_BRIGHT_WAKE_LOCK & ACQUIRE_CAUSES_WAKEUP to wake up screen
            try {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                @Suppress("DEPRECATION")
                val wl = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.ON_AFTER_RELEASE,
                    "PomodoroAlert::AlarmServiceWakeLock"
                )
                wl.acquire(10000L)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to acquire screen wakeup lock", e)
            }

            // B. Direct launch AlarmWakeUpActivity as backup to fullScreenIntent
            val fullScreenIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("isIndependentAlarm", true)
                putExtra("alarmRemark", remark)
                putExtra("alarmId", alarmId)
                putExtra("alarmType", alarmType)
                ringtoneUri?.let { putExtra("ringtoneUri", it) }
            }
            val creatorOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.app.ActivityOptions.makeBasic().apply {
                    setPendingIntentCreatorBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }.toBundle()
            } else {
                null
            }
            val pendingDirect = PendingIntent.getActivity(
                this,
                2005,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0),
                creatorOptions
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val senderOptions = android.app.ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    }.toBundle()
                    pendingDirect.send(this, 0, null, null, null, null, senderOptions)
                } else {
                    pendingDirect.send()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AlarmWakeUpActivity via PendingIntent from service", e)
                try {
                    startActivity(fullScreenIntent)
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to start AlarmWakeUpActivity directly from service fallback", ex)
                }
            }
        }

        if (voiceMode == "TTS" && voiceText.isNotBlank()) {
            startTtsLoop(voiceText)
        }

        // 2. 异步处理数据库状态更新与铃声播放，避免阻塞主线程
        serviceScope.launch {
            if (alarmId != null && alarmId != "temp_snooze") {
                launch(Dispatchers.IO) {
                    try {
                        val alarm = alarmDao.getById(alarmId)
                        if (alarm != null) {
                            if (alarm.repeatDays == 0) {
                                // 单次闹钟：响铃后禁用
                                alarmDao.setEnabled(alarmId, false)
                            } else {
                                // 重复闹钟：调度下一次触发
                                AlarmScheduler.scheduleAlarm(this@AlarmService, alarm)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reschedule or update alarm state: $alarmId", e)
                    }
                }
            }

            playAlarmRingtoneAsync(ringtoneUri, voiceMode, audioUri)
        }

        return START_STICKY
    }

    private suspend fun playAlarmRingtoneAsync(customUri: String?, voiceMode: String, audioUri: String?) {
        if (voiceMode == "AUDIO" && !audioUri.isNullOrBlank()) {
            playLocalRingtoneAsync(audioUri)
            return
        }

        val source = try {
            configRepo.ringtoneSource.first()
        } catch (e: Exception) {
            "local"
        }

        if (source == "built_in") {
            val fileName = try {
                configRepo.builtInRingtone.first()
            } catch (e: Exception) {
                "alert.mp3"
            }
            try {
                withContext(Dispatchers.IO) {
                    val mp = MediaPlayer().apply {
                        val assetFileDescriptor = assets.openFd(fileName)
                        setDataSource(
                            assetFileDescriptor.fileDescriptor,
                            assetFileDescriptor.startOffset,
                            assetFileDescriptor.length
                        )
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = true
                        prepare()
                    }
                    mediaPlayer = mp
                    mp.start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed playing built-in ringtone: $fileName, fallback to default", e)
                playLocalRingtoneAsync(customUri)
            }
        } else {
            playLocalRingtoneAsync(customUri)
        }
    }

    private suspend fun playLocalRingtoneAsync(customUri: String?) {
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
            withContext(Dispatchers.IO) {
                val mp = MediaPlayer().apply {
                    if (alarmUri.scheme == "file") {
                        val path = alarmUri.path
                        if (!path.isNullOrBlank()) {
                            setDataSource(path)
                        } else {
                            setDataSource(this@AlarmService, alarmUri)
                        }
                    } else {
                        setDataSource(this@AlarmService, alarmUri)
                    }
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                }
                mediaPlayer = mp
                mp.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing local ringtone: $customUri, fallback to system alarm", e)
            if (!customUri.isNullOrBlank()) {
                playLocalRingtoneAsync(null)
            }
        }
    }

    private fun startTtsLoop(text: String) {
        Log.d(TAG, "startTtsLoop: starting TTS loop with text: $text")
        ttsJob?.cancel()
        ttsJob = serviceScope.launch {
            val initialized = initTtsSuspend()
            Log.d(TAG, "startTtsLoop: initTtsSuspend returned: $initialized")
            if (initialized) {
                while (true) {
                    speakTts(text)
                    kotlinx.coroutines.delay(8000L)
                }
            } else {
                Log.e(TAG, "startTtsLoop: TTS failed to initialize")
            }
        }
    }

    private suspend fun initTtsSuspend(): Boolean = withContext(Dispatchers.Main) {
        Log.d(TAG, "initTtsSuspend: creating TextToSpeech instance using applicationContext")
        kotlin.coroutines.suspendCoroutine { continuation ->
            var resolved = false
            try {
                var localTts: TextToSpeech? = null
                localTts = TextToSpeech(applicationContext) { status ->
                    Log.d(TAG, "TextToSpeech init callback status: $status")
                    if (status == TextToSpeech.SUCCESS) {
                        localTts?.let { t ->
                            try {
                                Log.d(TAG, "Default TTS engine: ${t.defaultEngine}")
                                for (eng in t.engines) {
                                    Log.d(TAG, "Available Engine: name=${eng.name}, label=${eng.label}")
                                }
                                val voices = t.voices
                                if (voices != null) {
                                    Log.d(TAG, "Voices count: ${voices.size}")
                                    for (voice in voices) {
                                        val loc = voice.locale
                                        Log.d(TAG, "Voice: ${voice.name}, localeStr: $loc, lang: ${loc?.language}, country: ${loc?.country}, variant: ${loc?.variant}, tag: ${loc?.toLanguageTag()}")
                                    }
                                } else {
                                    Log.d(TAG, "Voices set is null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error logging engines/voices info", e)
                            }
                        }
                        if (!resolved) {
                            resolved = true
                            continuation.resume(true)
                        }
                    } else {
                        Log.e(TAG, "TextToSpeech init failed with status: $status")
                        if (!resolved) {
                            resolved = true
                            continuation.resume(false)
                        }
                    }
                }
                tts = localTts
            } catch (e: Exception) {
                Log.e(TAG, "Exception during TextToSpeech creation", e)
                if (!resolved) {
                    resolved = true
                    continuation.resume(false)
                }
            }
        }
    }

    private suspend fun speakTts(text: String) {
        withContext(Dispatchers.Main) {
            Log.d(TAG, "speakTts: attempting to speak text: $text")
            tts?.let { t ->
                var langSupported = false

                // 0. 检查当前引擎语言是否已经是中文
                try {
                    val currentLocale = t.language
                    Log.d(TAG, "speakTts: current locale = $currentLocale, language = ${currentLocale?.language}")
                    if (currentLocale != null) {
                        val lang = currentLocale.language
                        if (lang.equals("zh", ignoreCase = true) || 
                            lang.equals("chn", ignoreCase = true) || 
                            lang.equals("zho", ignoreCase = true) || 
                            currentLocale.toString().contains("zh", ignoreCase = true) ||
                            currentLocale.toString().contains("chn", ignoreCase = true)) {
                            Log.d(TAG, "speakTts: Current locale is already Chinese, skipping setup.")
                            langSupported = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking current locale", e)
                }

                // 1. 尝试使用 voices 列表匹配中文发音人
                if (!langSupported) {
                    try {
                        val voices = t.voices
                        if (!voices.isNullOrEmpty()) {
                            val chineseVoice = voices.firstOrNull { voice ->
                                voice.name.contains("中文") || 
                                voice.name.contains("Chinese", ignoreCase = true) ||
                                voice.locale.language.equals("zh", ignoreCase = true) ||
                                voice.locale.language.equals("chn", ignoreCase = true) ||
                                voice.locale.country.equals("chn", ignoreCase = true) ||
                                voice.locale.toString().contains("chn", ignoreCase = true) ||
                                voice.locale.toString().contains("zh", ignoreCase = true)
                            }
                            if (chineseVoice != null) {
                                Log.d(TAG, "speakTts: found Chinese voice: ${chineseVoice.name}, locale: ${chineseVoice.locale}, setting it.")
                                val setVoiceResult = t.setVoice(chineseVoice)
                                Log.d(TAG, "speakTts: setVoice result = $setVoiceResult")
                                if (setVoiceResult == TextToSpeech.SUCCESS) {
                                    langSupported = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting TTS voice", e)
                    }
                }

                // 2. 如果 setVoice 失败或不支持，尝试通过 Locale 列表设置
                if (!langSupported) {
                    val localesToTry = listOf(
                        Locale.getDefault(),
                        Locale.SIMPLIFIED_CHINESE,
                        Locale.CHINA,
                        Locale.CHINESE,
                        Locale("zh", "CN"),
                        Locale("chn"),
                        Locale("zh"),
                        Locale("zh", "HK"),
                        Locale("zh", "TW")
                    )
                    for (loc in localesToTry) {
                        val result = t.setLanguage(loc)
                        Log.d(TAG, "speakTts: setLanguage(${loc}) result = $result")
                        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            langSupported = true
                            break
                        }
                    }
                }

                if (!langSupported) {
                    Log.w(TAG, "speakTts: Chinese language setup failed. Attempting fallback speak with default engine language.")
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                t.setAudioAttributes(audioAttributes)
                val speakResult = t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AlarmSpeech")
                Log.d(TAG, "speakTts: speak command returned: $speakResult")
            } ?: Log.e(TAG, "speakTts: tts instance is null")
        }
    }

    private fun stopAndReleaseResources() {
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

        try {
            ttsJob?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ttsJob = null

        try {
            tts?.let {
                it.stop()
                it.shutdown()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tts = null
    }

    private fun buildNotification(
        alarmId: String?,
        remark: String,
        ringtoneUri: String?,
        alarmType: String,
        lockScreenEnabled: Boolean = true
    ): Notification {
        val fullScreenIntent = Intent(this, AlarmWakeUpActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("isIndependentAlarm", true)
            putExtra("alarmRemark", remark)
            putExtra("alarmId", alarmId)
            putExtra("alarmType", alarmType)
            ringtoneUri?.let { putExtra("ringtoneUri", it) }
        }

        val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic()
                .setPendingIntentCreatorBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }

        val pending = PendingIntent.getActivity(
            this,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0),
            bundle
        )

        // Action button to stop alarm
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPending = PendingIntent.getService(
            this,
            2002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("独立闹钟")
            .setContentText(remark)
            .setContentIntent(pending)  // 点击通知条始终打开界面
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 500, 500)) // 必须有振动或声音才能触发全屏 Intent 悬浮窗
            .addAction(R.drawable.ic_notification, "关闭", stopPending)

        if (lockScreenEnabled) {
            builder.setFullScreenIntent(pending, true)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "独立闹钟后台服务",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于保证后台闹钟正常响铃"
                setBypassDnd(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(3002)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopAndReleaseResources()
        serviceScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
