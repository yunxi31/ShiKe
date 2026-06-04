package com.pomodoroalert.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.data.AlarmEntity
import com.pomodoroalert.ui.RingtoneCopyHelper
import com.pomodoroalert.ui.localization.LocalLocalization
import com.pomodoroalert.ui.viewmodel.AlarmListViewModel

// Design Tokens (Matching AlarmScreen.kt)
private val DarkBrand = Color(0xFF1B1D21)
private val LightBrand = Color(0xFF808191)
private val ActiveColor = Color(0xFF6C5DD3)
private val PageBg = Color(0xFFF7F8FC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    navController: NavController,
    alarmId: String,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loc = LocalLocalization.current
    var alarmEntity by remember { mutableStateOf<AlarmEntity?>(null) }

    LaunchedEffect(alarmId) {
        alarmEntity = viewModel.getAlarmById(alarmId)
    }

    val alarm = alarmEntity ?: return // Loading state fallback

    var hour by remember(alarm) { mutableIntStateOf(alarm.hour) }
    var minute by remember(alarm) { mutableIntStateOf(alarm.minute) }
    var remark by remember(alarm) { mutableStateOf(alarm.remark) }
    var lockScreenEnabled by remember(alarm) { mutableStateOf(alarm.lockScreenEnabled) }
    var ringtoneEnabled by remember(alarm) { mutableStateOf(alarm.ringtoneEnabled) }
    var ringtoneUri by remember(alarm) { mutableStateOf<String?>(alarm.ringtoneUri) }
    var voiceMode by remember(alarm) { mutableStateOf(alarm.voiceMode) }
    var voiceText by remember(alarm) { mutableStateOf(alarm.voiceText) }
    var audioUri by remember(alarm) { mutableStateOf<String?>(alarm.audioUri) }

    // ---------- Ringtone picker launcher ----------
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            val isSystem = uri.toString().contains("content://settings/") ||
                    uri.toString().contains("content://media/internal/") ||
                    uri.toString().contains("content://settings/system/alarm_alert")
            ringtoneUri = if (isSystem) {
                uri.toString()
            } else {
                val copied = RingtoneCopyHelper.copyRingtoneToInternal(context, uri, alarm.alarmId)
                if (copied != null) {
                    val name = RingtoneCopyHelper.getRingtoneDisplayName(context, uri)
                    "$copied|$name"
                } else {
                    uri.toString()
                }
            }
        } else {
            ringtoneUri = null
        }
    }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var pendingIntent by remember { mutableStateOf<Intent?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingIntent?.let { ringtoneLauncher.launch(it) }
        }
        pendingIntent = null
    }

    val launchRingtonePicker = { intent: Intent ->
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            ringtoneLauncher.launch(intent)
        } else {
            pendingIntent = intent
            permissionLauncher.launch(permission)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = loc.editAlarmTitle, fontWeight = FontWeight.Bold, color = DarkBrand) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = loc.backDescription, tint = DarkBrand)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PageBg)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card for Time Picker
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "时间", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkBrand)
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ActiveColor.copy(alpha = 0.08f))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> hour = h; minute = m },
                                    hour, minute, true
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Card for basic settings
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Remark
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text(loc.remarkLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActiveColor,
                            focusedLabelColor = ActiveColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Lock Screen switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = loc.lockScreenEnabledTitle, fontWeight = FontWeight.Medium, color = DarkBrand)
                            Text(text = loc.lockScreenEnabledDesc, color = LightBrand, fontSize = 12.sp)
                        }
                        Switch(
                            checked = lockScreenEnabled,
                            onCheckedChange = { lockScreenEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ActiveColor
                            )
                        )
                    }
                }
            }

            // Card for voice broadcast configuration
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "语音播报设置", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkBrand)

                    // Voice Mode selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf("NONE" to "不播报", "TTS" to "文字转语音", "AUDIO" to "自定义音频")
                        modes.forEach { (mode, label) ->
                            val selected = voiceMode == mode
                            OutlinedButton(
                                onClick = { voiceMode = mode },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) ActiveColor.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (selected) ActiveColor else LightBrand
                                ),
                                modifier = Modifier.weight(1f),
                                border = ButtonDefaults.outlinedButtonBorder.let {
                                    if (selected) androidx.compose.foundation.BorderStroke(1.dp, ActiveColor) else it
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(text = label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    if (voiceMode == "TTS") {
                        OutlinedTextField(
                            value = voiceText,
                            onValueChange = { voiceText = it },
                            label = { Text("TTS播报文字") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ActiveColor,
                                focusedLabelColor = ActiveColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (voiceMode == "AUDIO") {
                        val selectAudioLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            val uri: Uri? = result.data?.data
                            if (uri != null) {
                                val name = RingtoneCopyHelper.getRingtoneDisplayName(context, uri)
                                val copied = RingtoneCopyHelper.copyRingtoneToInternal(context, uri, "${alarm.alarmId}_audio")
                                if (copied != null) {
                                    audioUri = "$copied|$name"
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "audio/*"
                                }
                                selectAudioLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            val displayAudio = if (audioUri.isNullOrBlank()) {
                                "选择自定义音频文件"
                            } else {
                                if (audioUri!!.contains("|")) audioUri!!.split("|")[1] else audioUri!!
                            }
                            Text(text = displayAudio)
                        }
                    }
                }
            }

            // Card for ringtone configuration
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "响铃铃声", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkBrand)
                        Switch(
                            checked = ringtoneEnabled,
                            onCheckedChange = { ringtoneEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ActiveColor
                            )
                        )
                    }

                    if (ringtoneEnabled) {
                        OutlinedButton(
                            onClick = {
                                val pickIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, loc.selectRingtoneTitle)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    ringtoneUri?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it)) }
                                }
                                launchRingtonePicker(pickIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ActiveColor)
                        ) {
                            Icon(Icons.Filled.MusicNote, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            val displayRingtone = if (ringtoneUri.isNullOrBlank()) {
                                loc.selectRingtoneTitle
                            } else {
                                if (ringtoneUri!!.contains("|")) ringtoneUri!!.split("|")[1] else ringtoneUri!!
                            }
                            Text(text = displayRingtone)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save & Cancel Actions
            Button(
                onClick = {
                    val updated = alarm.copy(
                        hour = hour,
                        minute = minute,
                        remark = remark,
                        lockScreenEnabled = lockScreenEnabled,
                        ringtoneEnabled = ringtoneEnabled,
                        ringtoneUri = ringtoneUri,
                        voiceMode = voiceMode,
                        voiceText = voiceText,
                        audioUri = audioUri
                    )
                    viewModel.updateAlarm(updated)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ActiveColor),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(text = loc.saveBtn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
