package com.pomodoroalert.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.viewmodel.SettingsViewModel
import com.pomodoroalert.ui.localization.LocalLocalization
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.media.MediaPlayer
import com.pomodoroalert.ui.SystemPermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val earphoneMode by viewModel.earphoneMode.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val ringtoneSource by viewModel.ringtoneSource.collectAsState()
    val builtInRingtone by viewModel.builtInRingtone.collectAsState()
    val motivationalQuote by viewModel.motivationalQuote.collectAsState()
    val loc = LocalLocalization.current

    val pageBackground = MaterialTheme.colorScheme.background
    val brand = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surface
    val textMain = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val isDark = pageBackground == Color(0xFF1B1D21)
    val greenColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)

    val context = LocalContext.current
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isIgnoringBattery by remember { mutableStateOf(SystemPermissionHelper.isIgnoringBatteryOptimizations(context)) }
    var areNotificationsEnabled by remember { mutableStateOf(SystemPermissionHelper.areNotificationsEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isIgnoringBattery = SystemPermissionHelper.isIgnoringBatteryOptimizations(context)
                areNotificationsEnabled = SystemPermissionHelper.areNotificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.stop()
            previewPlayer?.release()
        }
    }

    fun playPreview(fileName: String) {
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
            previewPlayer = MediaPlayer().apply {
                val assetFileDescriptor = context.assets.openFd(fileName)
                setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = pageBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(loc.settingsTitle, fontWeight = FontWeight.Bold, color = textMain, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = loc.backDescription, tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(loc.earphoneModeTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(loc.earphoneModeDesc, fontSize = 12.sp, color = textMuted)
                        }
                        Switch(
                            checked = earphoneMode,
                            onCheckedChange = { viewModel.setEarphoneMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = brand,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.languageSettingTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.languageSettingDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LanguageOption(
                                label = "中文",
                                isSelected = currentLang == "zh",
                                onClick = { viewModel.setLanguage("zh") },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageOption(
                                label = "English",
                                isSelected = currentLang == "en",
                                onClick = { viewModel.setLanguage("en") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ringtone Source Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.ringtoneModeTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.ringtoneModeDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setRingtoneSource("built_in") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ringtoneSource == "built_in") brand else brand.copy(alpha = 0.08f),
                                    contentColor = if (ringtoneSource == "built_in") MaterialTheme.colorScheme.onPrimary else brand
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = loc.ringtoneModeBuiltIn, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { 
                                    viewModel.setRingtoneSource("local") 
                                    previewPlayer?.stop()
                                    previewPlayer?.release()
                                    previewPlayer = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ringtoneSource == "local") brand else brand.copy(alpha = 0.08f),
                                    contentColor = if (ringtoneSource == "local") MaterialTheme.colorScheme.onPrimary else brand
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = loc.ringtoneModeLocal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Built-in Ringtone Selection Card (only show if ringtoneSource is built_in)
                if (ringtoneSource == "built_in") {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                            Text(loc.selectBuiltInRingtoneTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val ringtoneFiles = remember {
                                try {
                                    val files = context.assets.list("")
                                    android.util.Log.d("PomodoroAssets", "All assets files in root: " + files?.joinToString())
                                    files?.filter { 
                                        it.endsWith(".mp3", ignoreCase = true) || 
                                        it.endsWith(".wav", ignoreCase = true) || 
                                        it.endsWith(".ogg", ignoreCase = true) ||
                                        it.endsWith(".m4a", ignoreCase = true) ||
                                        it.endsWith(".aac", ignoreCase = true)
                                    }?.sorted() ?: emptyList()
                                } catch (e: Exception) {
                                    android.util.Log.e("PomodoroAssets", "Failed listing assets", e)
                                    emptyList()
                                }
                            }
                            
                            if (ringtoneFiles.isEmpty()) {
                                Text(text = "No ringtones found in assets", color = textMuted, fontSize = 14.sp)
                            } else {
                                // Auto-correct selection if current is not in the list
                                if (builtInRingtone !in ringtoneFiles) {
                                    viewModel.setBuiltInRingtone(ringtoneFiles.first())
                                }
                                
                                ringtoneFiles.forEach { fileName ->
                                    val displayName = when (fileName) {
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
                                    val isSelected = builtInRingtone == fileName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { 
                                                viewModel.setBuiltInRingtone(fileName) 
                                                playPreview(fileName)
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = brand,
                                                unselectedColor = textMuted
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = displayName,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) brand else textMain,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    viewModel.setBuiltInRingtone(fileName)
                                                    playPreview(fileName)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Custom Motivational Quote Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.motivationalQuoteTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.motivationalQuoteDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = motivationalQuote,
                            onValueChange = { viewModel.setMotivationalQuote(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(loc.motivationalQuotePlaceholder, fontSize = 14.sp, color = textMuted) },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = brand,
                                unfocusedBorderColor = textMuted.copy(alpha = 0.2f),
                                focusedLabelColor = brand,
                                cursorColor = brand,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notification Permission Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.notificationsTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.notificationsDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = if (areNotificationsEnabled) greenColor else redColor
                            val badgeText = if (areNotificationsEnabled) loc.notificationsEnabled else loc.notificationsDisabled

                            Surface(
                                color = badgeColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!areNotificationsEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { SystemPermissionHelper.openAppDetailsSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = brand, contentColor = MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = loc.notificationsBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Battery Optimization Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.batteryOptimizationTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.batteryOptimizationDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = if (isIgnoringBattery) greenColor else redColor
                            val badgeText = if (isIgnoringBattery) loc.batteryOptimizationIgnored else loc.batteryOptimizationNotIgnored
                            
                            Surface(
                                color = badgeColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        if (!isIgnoringBattery) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { SystemPermissionHelper.requestIgnoreBatteryOptimizations(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = brand, contentColor = MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = loc.batteryOptimizationBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Auto Start Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.autoStartTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.autoStartDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { SystemPermissionHelper.openAutoStartSettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brand.copy(alpha = 0.08f),
                                contentColor = brand
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = loc.autoStartBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Lock Screen and Background Pop-up Permission Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.lockScreenPermTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.lockScreenPermDesc, fontSize = 12.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { SystemPermissionHelper.openAppDetailsSettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brand.copy(alpha = 0.08f),
                                contentColor = brand
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = loc.lockScreenPermBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brand = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) brand else brand.copy(alpha = 0.08f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else brand
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}


