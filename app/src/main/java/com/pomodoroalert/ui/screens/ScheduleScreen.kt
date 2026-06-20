package com.pomodoroalert.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.RingtoneCopyHelper
import com.pomodoroalert.ui.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val markdownText by viewModel.markdownText.collectAsState()
    val drafts by viewModel.drafts.collectAsState()
    
    var activeDraftIdForAudioPicker by remember { mutableStateOf<String?>(null) }

    fun handleSaveAndExit() {
        viewModel.saveAndApplySchedule(
            onSuccess = {
                Toast.makeText(context, "成功保存并激活作息闹钟", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    fun onBackClick() {
        viewModel.saveAndApplySchedule(
            onSuccess = {
                navController.popBackStack()
            },
            onError = { error ->
                if (markdownText.isNotBlank()) {
                    Toast.makeText(context, "已保存有效作息。部分错误: $error", Toast.LENGTH_SHORT).show()
                }
                navController.popBackStack()
            }
        )
    }

    // Intercept system back button
    BackHandler(onBack = { onBackClick() })

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val draftId = activeDraftIdForAudioPicker
        if (uri != null && draftId != null) {
            val copiedUri = RingtoneCopyHelper.copyRingtoneToInternal(context, uri, draftId)
            val displayName = RingtoneCopyHelper.getRingtoneDisplayName(context, uri)
            val finalUriStr = if (copiedUri != null) "$copiedUri|$displayName" else "$uri|$displayName"
            viewModel.updateDraftAudio(draftId, finalUriStr, displayName)
        }
        activeDraftIdForAudioPicker = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("作息提醒设置", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (drafts.isEmpty()) {
                                Toast.makeText(context, "请先按格式输入有效的作息时间安排", Toast.LENGTH_SHORT).show()
                            } else {
                                handleSaveAndExit()
                            }
                        }
                    ) {
                        Text(
                            text = "保存",
                            color = if (drafts.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (markdownText.isNotBlank() && drafts.isEmpty()) {
                        Text(
                            text = "未识别到有效作息，请检查格式如：06:00 - 06:40 起床",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else if (drafts.isNotEmpty()) {
                        Text(
                            text = "已识别出 ${drafts.size} 个作息段闹钟",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (drafts.isEmpty()) {
                                Toast.makeText(context, "请先按格式输入有效的作息时间安排", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveAndApplySchedule(
                                    onSuccess = {
                                        Toast.makeText(context, "成功导入并激活作息闹钟提醒", Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (drafts.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("生成并激活作息闹钟", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Markdown Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "粘贴或编辑作息安排",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "支持格式: HH:mm - HH:mm 事项安排。每一项占一行，作息安排必须是连续的。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = markdownText,
                            onValueChange = {
                                viewModel.updateMarkdownText(it)
                            },
                            placeholder = {
                                Text("06:00 - 06:40 起床洗漱、打八部金刚功\n06:40 - 07:10 早餐")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (drafts.isEmpty()) {
                                    Toast.makeText(context, "请先按格式输入有效的作息时间安排", Toast.LENGTH_SHORT).show()
                                } else {
                                    handleSaveAndExit()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (drafts.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("确定并应用当前作息", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Parsed list header
            if (drafts.isNotEmpty()) {
                item {
                    Text(
                        text = "识别出的闹钟提醒清单 (${drafts.size}个)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(drafts, key = { it.id }) { draft ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Time and action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(
                                        "%02d:%02d - %02d:%02d",
                                        draft.startHour, draft.startMinute,
                                        draft.endHour, draft.endMinute
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = draft.content,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false).padding(start = 12.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Enable Alarm Option ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "启用该时段闹铃",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "在此时间段是否触发闹铃和语音播报",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = draft.isEnabled,
                                    onCheckedChange = { viewModel.updateDraftEnabled(draft.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Play Ringtone Option ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "同时播放闹铃铃声",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "默认关闭（仅语音播报）。开启后将同时播放闹铃音乐",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = draft.ringtoneEnabled,
                                    onCheckedChange = { viewModel.updateDraftRingtoneEnabled(draft.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Lock Screen Setting Row ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "闹钟锁屏大屏提醒",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "响铃时强制亮屏并弹出全屏闹铃界面",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = draft.lockScreenEnabled,
                                    onCheckedChange = { viewModel.updateDraftLockScreenEnabled(draft.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("语音提醒设置", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Voice mode switch
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val isTTS = draft.voiceMode == "TTS"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isTTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
                                        .clickable { viewModel.updateDraftVoiceMode(draft.id, "TTS") }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                     Text(
                                         text = "文字播报 (TTS)",
                                         color = if (isTTS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                         fontSize = 13.sp,
                                         fontWeight = FontWeight.Medium
                                     )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isTTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
                                        .clickable { viewModel.updateDraftVoiceMode(draft.id, "AUDIO") }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "录音/音频",
                                        color = if (!isTTS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (draft.voiceMode == "TTS") {
                                OutlinedTextField(
                                    value = draft.voiceText,
                                    onValueChange = { viewModel.updateDraftVoiceText(draft.id, it) },
                                    placeholder = { Text("主人，该${draft.content}了") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    maxLines = 3
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            activeDraftIdForAudioPicker = draft.id
                                            audioPickerLauncher.launch("audio/*")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("选择音频文件", color = Color.White, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = draft.audioFileName ?: "未选择音频 (响铃时播报TTS)",
                                        fontSize = 13.sp,
                                        color = if (draft.audioFileName != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
