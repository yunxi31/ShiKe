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

// Design Tokens
private val PageBackground = Color(0xFFF7F8FC)
private val Brand = Color(0xFF6C5DD3)
private val CardBg = Color.White
private val TextMain = Color(0xFF1B1D21)
private val TextMuted = Color(0xFF808191)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val earphoneMode by viewModel.earphoneMode.collectAsState()
    val defaultPomodoro by viewModel.defaultPomodoro.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val loc = LocalLocalization.current

    Surface(modifier = Modifier.fillMaxSize(), color = PageBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text(loc.settingsTitle, fontWeight = FontWeight.Bold, color = TextMain, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = loc.backDescription, tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(loc.earphoneModeTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(loc.earphoneModeDesc, fontSize = 12.sp, color = TextMuted)
                        }
                        Switch(
                            checked = earphoneMode,
                            onCheckedChange = { viewModel.setEarphoneMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Brand,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE2E2EA)
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.defaultPomodoroTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Spacer(modifier = Modifier.height(24.dp))
                        Slider(
                            value = defaultPomodoro.toFloat(),
                            onValueChange = { viewModel.setDefaultPomodoro(it.toInt()) },
                            valueRange = 1f..60f,
                            steps = 58,
                            colors = SliderDefaults.colors(
                                thumbColor = Brand,
                                activeTrackColor = Brand,
                                inactiveTrackColor = Brand.copy(alpha = 0.24f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loc.currentSettingMins(defaultPomodoro),
                            fontSize = 14.sp,
                            color = Brand,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(loc.languageSettingTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(loc.languageSettingDesc, fontSize = 12.sp, color = TextMuted)
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
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Brand else Brand.copy(alpha = 0.08f),
            contentColor = if (isSelected) Color.White else Brand
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}


