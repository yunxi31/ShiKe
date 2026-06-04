package com.pomodoroalert.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.data.AlarmEntity
import com.pomodoroalert.ui.viewmodel.AlarmListViewModel
import com.pomodoroalert.ui.localization.LocalLocalization
import com.pomodoroalert.ui.RingtoneCopyHelper

// ── Design Tokens ──────────────────────────────────────────────────────
private val DarkBrand = Color(0xFF1B1D21)
private val LightBrand = Color(0xFF808191)
private val ActiveColor = Color(0xFF6C5DD3)
private val InactiveColor = Color(0xFF808191).copy(alpha = 0.5f)
private val PageBg = Color(0xFFF7F8FC)

// ── Helper: 获取铃声标题 ──
@Composable
private fun ringtoneTitle(uriString: String?): String {
    val context = LocalContext.current
    val loc = LocalLocalization.current
    if (uriString.isNullOrBlank()) return loc.systemDefault
    if (uriString.contains("|")) {
        val parts = uriString.split("|")
        if (parts.size >= 2) {
            return parts[1]
        }
    }
    return remember(uriString, loc) {
        try {
            val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uriString))
            ringtone?.getTitle(context) ?: loc.customRingtone
        } catch (_: Exception) {
            loc.customRingtone
        }
    }
}

// ── Helper: 获取内置铃声显示名称 ──
@Composable
private fun getBuiltInRingtoneDisplayName(fileName: String): String {
    val loc = LocalLocalization.current
    return when (fileName) {
        "alert.mp3" -> loc.ringtoneAlertMp3
        "alert.wav" -> loc.ringtoneAlertWav
        else -> fileName.substringBeforeLast(".")
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val ringtoneSource by viewModel.ringtoneSource.collectAsState()
    val builtInRingtone by viewModel.builtInRingtone.collectAsState()
    val context = LocalContext.current

    // ── Dialog state ──
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    // ── 铃声选择器 (用于已有闹钟) ──
    var alarmForRingtonePick by remember { mutableStateOf<AlarmEntity?>(null) }
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        alarmForRingtonePick?.let { alarm ->
            val processedRingtoneUri = if (uri != null) {
                val isSystem = uri.toString().contains("content://settings/") || 
                               uri.toString().contains("content://media/internal/") ||
                               uri.toString().contains("content://settings/system/alarm_alert")
                if (isSystem) {
                    uri.toString()
                } else {
                    val copiedUri = RingtoneCopyHelper.copyRingtoneToInternal(context, uri, alarm.alarmId)
                    if (copiedUri != null) {
                        val displayName = RingtoneCopyHelper.getRingtoneDisplayName(context, uri)
                        "$copiedUri|$displayName"
                    } else {
                        uri.toString()
                    }
                }
            } else {
                null
            }
            viewModel.updateAlarm(alarm.copy(ringtoneUri = processedRingtoneUri))
        }
        alarmForRingtonePick = null
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var pendingIntentForRingtonePick by remember { mutableStateOf<Intent?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingIntentForRingtonePick?.let {
            ringtoneLauncher.launch(it)
        }
        pendingIntentForRingtonePick = null
    }

    val launchRingtonePicker = { intent: Intent ->
        if (ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
            ringtoneLauncher.launch(intent)
        } else {
            pendingIntentForRingtonePick = intent
            permissionLauncher.launch(permissionToRequest)
        }
    }
    val loc = LocalLocalization.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("普通闹钟", "作息提醒")

    val filteredAlarms = remember(alarms, selectedTabIndex) {
        if (selectedTabIndex == 0) {
            alarms.filter { it.alarmType == "REGULAR" }
        } else {
            alarms.filter { it.alarmType == "SCHEDULE" }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PageBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──
            CenterAlignedTopAppBar(
                title = {
                    Text(loc.alarmListTitle, fontWeight = FontWeight.Bold, color = DarkBrand, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = loc.backDescription, tint = DarkBrand)
                    }
                },
                actions = {
                    if (selectedTabIndex == 0) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = loc.addAlarm, tint = DarkBrand)
                        }
                    } else {
                        IconButton(onClick = { navController.navigate("schedule") }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "作息提醒", tint = DarkBrand)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // ── Tab Tabs ──
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = ActiveColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                color = if (selectedTabIndex == index) ActiveColor else LightBrand
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Schedule Reminder Banner ──
                if (selectedTabIndex == 1) {
                    item {
                        ScheduleReminderBanner(onClick = { navController.navigate("schedule") })
                    }
                }

                if (filteredAlarms.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.NotificationsOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = InactiveColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (selectedTabIndex == 0) {
                                        "${loc.noAlarmsTitle}\n${loc.noAlarmsDesc}"
                                    } else {
                                        "暂无作息提醒\n点击上方卡片编写你的作息表"
                                    },
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAlarms, key = { it.alarmId }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onEdit = { navController.navigate("alarm_detail/${alarm.alarmId}") },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }

    // ── Add Alarm Dialog ──
    if (showAddDialog) {
        AddAlarmDialog(
            ringtoneSource = ringtoneSource,
            builtInRingtone = builtInRingtone,
            onDismiss = { showAddDialog = false },
            onConfirm = { hour, minute, remark, ringtoneUri, lockScreenEnabled ->
                viewModel.addAlarm(hour, minute, remark, ringtoneUri, lockScreenEnabled)
                showAddDialog = false
            }
        )
    }

    // ── Edit Remark Dialog ──
    editingAlarm?.let { alarm ->
        EditRemarkDialog(
            currentRemark = alarm.remark,
            onDismiss = { editingAlarm = null },
            onConfirm = { newRemark ->
                viewModel.updateAlarm(alarm.copy(remark = newRemark))
                editingAlarm = null
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Alarm Card
// ═══════════════════════════════════════════════════════════════════════
@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) DarkBrand else Color(0xFF808191).copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    if (alarm.alarmType == "SCHEDULE") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(ActiveColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "作息",
                                color = ActiveColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (alarm.remark.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alarm.remark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (alarm.isEnabled) DarkBrand else Color(0xFF808191).copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF7A8A).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ActiveColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE2E2EA)
                    )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Add Alarm Dialog — 选时间 → 填备注 + 选铃声
// ═══════════════════════════════════════════════════════════════════════
@Composable
private fun AddAlarmDialog(
    ringtoneSource: String,
    builtInRingtone: String,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, remark: String, ringtoneUri: String?, lockScreenEnabled: Boolean) -> Unit
) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    var selectedHour by remember { mutableIntStateOf(calendar.get(java.util.Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(java.util.Calendar.MINUTE)) }
    var remark by remember { mutableStateOf("") }
    var lockScreenEnabled by remember { mutableStateOf(true) }
    var ringtoneUri by remember { mutableStateOf<String?>(null) }
    var timeChosen by remember { mutableStateOf(false) }

    // 铃声选择器
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            val isSystem = uri.toString().contains("content://settings/") || 
                           uri.toString().contains("content://media/internal/") ||
                           uri.toString().contains("content://settings/system/alarm_alert")
            if (isSystem) {
                ringtoneUri = uri.toString()
            } else {
                val tempId = java.util.UUID.randomUUID().toString()
                val copiedUri = RingtoneCopyHelper.copyRingtoneToInternal(context, uri, tempId)
                if (copiedUri != null) {
                    val displayName = RingtoneCopyHelper.getRingtoneDisplayName(context, uri)
                    ringtoneUri = "$copiedUri|$displayName"
                } else {
                    ringtoneUri = uri.toString()
                }
            }
        } else {
            ringtoneUri = null
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var pendingIntentForRingtonePick by remember { mutableStateOf<Intent?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingIntentForRingtonePick?.let {
            ringtoneLauncher.launch(it)
        }
        pendingIntentForRingtonePick = null
    }

    val launchRingtonePicker = { intent: Intent ->
        if (ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
            ringtoneLauncher.launch(intent)
        } else {
            pendingIntentForRingtonePick = intent
            permissionLauncher.launch(permissionToRequest)
        }
    }

    if (!timeChosen) {
        // Use platform TimePickerDialog
        val dialog = remember {
            TimePickerDialog(context, { _, h, m ->
                selectedHour = h
                selectedMinute = m
                timeChosen = true
            }, selectedHour, selectedMinute, true)
        }
        LaunchedEffect(Unit) {
            dialog.setOnCancelListener { onDismiss() }
            dialog.show()
        }
    } else {
        // Then show remark + ringtone dialog
        val dialogLoc = LocalLocalization.current
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "${dialogLoc.newAlarmTitle} ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                    fontWeight = FontWeight.Bold,
                    color = DarkBrand
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text(dialogLoc.remarkLabel) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActiveColor,
                            focusedLabelColor = ActiveColor
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 锁屏闹钟开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dialogLoc.lockScreenEnabledTitle,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkBrand,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dialogLoc.lockScreenEnabledDesc,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = lockScreenEnabled,
                            onCheckedChange = { lockScreenEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ActiveColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE2E2EA)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 铃声选择按钮
                    val isRingtoneClickable = ringtoneSource != "built_in"
                    OutlinedButton(
                        onClick = {
                            if (isRingtoneClickable) {
                                val pickIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, dialogLoc.selectRingtoneTitle)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    if (!ringtoneUri.isNullOrBlank()) {
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            Uri.parse(ringtoneUri)
                                        )
                                    }
                                }
                                launchRingtonePicker(pickIntent)
                            }
                        },
                        enabled = isRingtoneClickable,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isRingtoneClickable) ActiveColor else LightBrand
                        )
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val displayName = if (ringtoneSource == "built_in") {
                            getBuiltInRingtoneDisplayName(builtInRingtone)
                        } else {
                            ringtoneTitle(ringtoneUri)
                        }
                        
                        Text(
                            text = "${dialogLoc.ringtonePrefix}${displayName}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(selectedHour, selectedMinute, remark, ringtoneUri, lockScreenEnabled) },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveColor)
                ) {
                    Text(dialogLoc.addBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(dialogLoc.cancelBtn) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Edit Remark Dialog
// ═══════════════════════════════════════════════════════════════════════
@Composable
private fun EditRemarkDialog(
    currentRemark: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentRemark) }
    val dialogLoc = LocalLocalization.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogLoc.editRemarkTitle, fontWeight = FontWeight.Bold, color = DarkBrand) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(dialogLoc.remarkLabel) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ActiveColor,
                    focusedLabelColor = ActiveColor
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveColor)
            ) {
                Text(dialogLoc.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dialogLoc.cancelBtn) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ── Schedule Reminder Banner Card ──
@Composable
fun ScheduleReminderBanner(onClick: () -> Unit) {
    val loc = LocalLocalization.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6C5DD3), Color(0xFF8B7CF0))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loc.scheduleBannerTitle,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "New",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = loc.scheduleBannerDesc,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF6C5DD3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = loc.scheduleBannerBtn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

