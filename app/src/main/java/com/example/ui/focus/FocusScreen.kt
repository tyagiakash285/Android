package com.example.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AreaBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val secondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
    val initialSeconds by viewModel.timerInitialSeconds.collectAsState()
    val activeTask by viewModel.activeFocusTask.collectAsState()
    val activeArea by viewModel.activeFocusArea.collectAsState()
    val distractionCount by viewModel.distractionCount.collectAsState()
    val focusSessions by viewModel.allFocusSessions.collectAsState()

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = remember(secondsRemaining, initialSeconds) {
        if (initialSeconds > 0) (secondsRemaining.toFloat() / initialSeconds.toFloat()) else 0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Mode Selector Presets ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = initialSeconds == 25 * 60,
                    onClick = { viewModel.setTimerPreset(25, activeTask, activeArea) },
                    label = { Text("25m Pomodoro", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = initialSeconds == 50 * 60,
                    onClick = { viewModel.setTimerPreset(50, activeTask, activeArea) },
                    label = { Text("50m Deep Work", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = initialSeconds == 15 * 60,
                    onClick = { viewModel.setTimerPreset(15, activeTask, activeArea) },
                    label = { Text("15m Sprint", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // --- 2. Big Animated Timer Circle ---
        item {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle track
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(LifeOsPrimary, DeepWorkPurple, LifeOsSecondary, LifeOsPrimary)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeTask,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AreaBadge(areaName = activeArea)
                }
            }
        }

        // --- 3. Controls (Play, Pause, Reset, Distractions) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                IconButton(
                    onClick = { viewModel.resetFocusTimer() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Main Action
                Button(
                    onClick = {
                        if (isRunning) viewModel.pauseFocusTimer() else viewModel.startFocusTimer()
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MedPriorityAmber else LifeOsPrimary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Log Distraction
                IconButton(
                    onClick = { viewModel.recordDistraction() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.NotificationsPaused, contentDescription = "Log Distraction")
                }
            }
        }

        if (distractionCount > 0) {
            item {
                Text(
                    text = "Distractions deflected: $distractionCount",
                    fontSize = 12.sp,
                    color = MedPriorityAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // --- 4. Deep Work Session Logs ---
        item {
            SectionHeader(title = "RECENT DEEP WORK LOGS", icon = "📊")
        }

        if (focusSessions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No completed focus sessions yet. Hit play to start!", fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(focusSessions.take(5), key = { it.id }) { session ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DeepWorkPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = DeepWorkPurple, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = session.taskTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AreaBadge(areaName = session.area)
                            }
                        }

                        Text(
                            text = "${session.durationMinutes} min",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LifeOsPrimary
                        )
                    }
                }
            }
        }
    }
}
