package com.pomodoroalert.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodoroalert.ui.localization.LocalLocalization
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val loc = LocalLocalization.current

    // Animations states
    val boxScale = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(30f) }
    val glowScale = remember { Animatable(0.5f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Animate checkbox box popping in
        boxScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = Easing { OvershootInterpolator(1.8f).getInterpolation(it) }
            )
        )

        // Step 2: Animate glowing background rising
        launch {
            glowAlpha.animateTo(0.15f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            glowScale.animateTo(1.3f, tween(1000, easing = FastOutSlowInEasing))
        }

        // Step 3: Satisfying checkmark animation
        checkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )

        // Step 4: Text fade-in and slide-up
        launch {
            textAlpha.animateTo(1f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
        }
        launch {
            textOffset.animateTo(0f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
        }

        // Step 5: Pulse the checkmark slightly when completed
        launch {
            boxScale.animateTo(1.1f, tween(150, easing = FastOutSlowInEasing))
            boxScale.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
        }

        // Extra delay to enjoy the animation, then proceed
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F8FC),
                        Color(0xFFEAEAFA)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Subtle glow aura behind checkbox
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(glowScale.value)
                        .alpha(glowAlpha.value)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6C5DD3),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }

                // Checkbox & Checkmark Canvas
                Canvas(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(boxScale.value)
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw rounded checkbox border
                    val strokeWidth = 6.dp.toPx()
                    val cornerRadius = 24.dp.toPx()
                    val checkboxPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = strokeWidth / 2,
                                    top = strokeWidth / 2,
                                    right = width - strokeWidth / 2,
                                    bottom = height - strokeWidth / 2
                                ),
                                radiusX = cornerRadius,
                                radiusY = cornerRadius
                            )
                        )
                    }

                    drawPath(
                        path = checkboxPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8C7DF3),
                                Color(0xFF6C5DD3)
                            )
                        ),
                        style = Stroke(
                            width = strokeWidth,
                            join = StrokeJoin.Round,
                            cap = StrokeCap.Round
                        )
                    )

                    // 2. Draw Checkmark path dynamically based on progress
                    val progress = checkProgress.value
                    if (progress > 0f) {
                        val checkPath = Path()

                        // Key points on 100x100 canvas:
                        // Start: (30, 50)
                        // Pivot: (45, 68)
                        // End: (74, 34)
                        val x0 = width * 0.30f
                        val y0 = height * 0.50f
                        val x1 = width * 0.45f
                        val y1 = height * 0.68f
                        val x2 = width * 0.74f
                        val y2 = height * 0.34f

                        val segment1Progress = (progress / 0.4f).coerceAtMost(1f)
                        val curX1 = x0 + (x1 - x0) * segment1Progress
                        val curY1 = y0 + (y1 - y0) * segment1Progress

                        checkPath.moveTo(x0, y0)
                        checkPath.lineTo(curX1, curY1)

                        if (progress > 0.4f) {
                            val segment2Progress = ((progress - 0.4f) / 0.6f).coerceAtMost(1f)
                            val curX2 = x1 + (x2 - x1) * segment2Progress
                            val curY2 = y1 + (y2 - y1) * segment2Progress
                            checkPath.lineTo(curX2, curY2)
                        }

                        drawPath(
                            path = checkPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF24E0A5),
                                    Color(0xFF00C9A7)
                                )
                            ),
                            style = Stroke(
                                width = 8.dp.toPx(),
                                join = StrokeJoin.Round,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name & Tagline fading in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset { IntOffset(0, textOffset.value.dp.roundToPx()) }
            ) {
                Text(
                    text = "时刻",
                    color = Color(0xFF1B1D21),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = loc.appTagline,
                    color = Color(0xFF808191),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
