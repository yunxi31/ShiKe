package com.pomodoroalert.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.ui.viewmodel.FocusViewModel
import com.pomodoroalert.ui.localization.LocalLocalization

// Design Tokens adapted to HomeScreen style
private val PageBackground = Color(0xFFF7F8FC)
private val Brand = Color(0xFF6C5DD3)      // Main purple-blue
private val BrandLight = Color(0xFF8B7CF0) // Light purple-blue for gradient
private val TextMain = Color(0xFF1B1D21)
private val TextMuted = Color(0xFF808191)
private val ErrorRed = Color(0xFFFF7A8A)   // Match notification/work red

@Composable
fun FocusScreen(navController: NavController, taskId: String? = null) {
    val viewModel: FocusViewModel = hiltViewModel()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()
    val totalTime = currentTask?.duration ?: 1L
    val loc = LocalLocalization.current

    val progress = if (totalTime > 0) {
        (remainingTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.startFocus(taskId)
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(PageBackground, Color(0xFFEFF0F9))
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Area
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = loc.focusingTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = currentTask?.taskName ?: loc.noTaskSelected,
                            style = MaterialTheme.typography.titleMedium,
                            color = Brand,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Timer Area
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(320.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        drawArc(
                            color = Brand.copy(alpha = 0.1f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size.width, size.height),
                            topLeft = Offset(0f, 0f)
                        )
                        drawArc(
                            brush = Brush.linearGradient(
                                colors = listOf(BrandLight, Brand)
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size.width, size.height),
                            topLeft = Offset(0f, 0f)
                        )
                    }

                    val seconds = (remainingTime / 1000) % 60
                    val minutes = (remainingTime / 1000) / 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                // Bottom Control Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                viewModel.abandonTask()
                                navController.popBackStack("home", inclusive = false)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ErrorRed.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = loc.abandon, tint = ErrorRed, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(loc.abandon, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.completeTask()
                                navController.popBackStack("home", inclusive = false)
                            },
                            containerColor = Brand,
                            contentColor = Color.White,
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = loc.complete, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(loc.complete, style = MaterialTheme.typography.labelMedium, color = TextMain, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.postpone(10) },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brand.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Filled.MoreTime, contentDescription = loc.postponeOption, tint = Brand, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(loc.postponeOption, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                }
            }
        }
    }
}
