package com.example.ui.analytics

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ProductivityBreakdown
import com.example.domain.ProductivityPattern
import com.example.ui.MainViewModel
import com.example.ui.components.ProgressBarCustom
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigateToCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val breakdown by viewModel.productivityBreakdown.collectAsState()
    val patterns by viewModel.patterns.collectAsState()
    val focusSessions by viewModel.allFocusSessions.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    val totalDeepWorkHours = remember(focusSessions) {
        val totalMins = focusSessions.sumOf { it.durationMinutes }
        "${totalMins / 60}h ${totalMins % 60}m"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Productivity Score Banner ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    LifeOsPrimary.copy(alpha = 0.12f),
                                    Color(0xFF10B981).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PRODUCTIVITY INDEX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = LifeOsPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "High Execution State",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(LifeOsPrimary)
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${breakdown.totalScore}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "/ 100",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Transparent Factor Weights
                        Text(
                            text = "TRANSPARENT SCORING BREAKDOWN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProductivityFactorRow(
                            title = "Task Completion",
                            weight = "30%",
                            score = breakdown.taskCompletionScore,
                            maxScore = 30,
                            detail = "${breakdown.tasksCompletedCount}/${breakdown.tasksPlannedCount} tasks completed"
                        )

                        ProductivityFactorRow(
                            title = "Habit Consistency",
                            weight = "25%",
                            score = breakdown.habitConsistencyScore,
                            maxScore = 25,
                            detail = "${breakdown.habitsCompletedToday}/${breakdown.totalHabitsCount} habits logged"
                        )

                        ProductivityFactorRow(
                            title = "Deep Focus Hours",
                            weight = "20%",
                            score = breakdown.focusTimeScore,
                            maxScore = 20,
                            detail = "${breakdown.focusMinutesToday} / 120 min target"
                        )

                        ProductivityFactorRow(
                            title = "Goal Milestone Progress",
                            weight = "15%",
                            score = breakdown.goalProgressScore,
                            maxScore = 15,
                            detail = "${breakdown.averageGoalProgress}% average roadmaps"
                        )

                        ProductivityFactorRow(
                            title = "Daily Routine Execution",
                            weight = "10%",
                            score = breakdown.routineScore,
                            maxScore = 10,
                            detail = "Morning Launchpad completed"
                        )
                    }
                }
            }
        }

        // --- 2. AI Pattern Detection ---
        item {
            SectionHeader(
                title = "AI PATTERN DETECTION",
                icon = "🤖",
                actionText = "Full Audit",
                onActionClick = onNavigateToCoach
            )
        }

        items(patterns) { pattern ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = pattern.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pattern.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pattern.observation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LifeOsPrimary.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "💡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pattern.recommendation,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Weekly Review CTA ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📊", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Weekly AI Review Ready",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Generate a comprehensive evaluation of your progress",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToCoach,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LifeOsPrimary)
                    ) {
                        Text("Generate Weekly Review with AI Coach", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityFactorRow(
    title: String,
    weight: String,
    score: Int,
    maxScore: Int,
    detail: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "($weight)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "$score / $maxScore pts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LifeOsPrimary
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        ProgressBarCustom(
            progressPercent = if (maxScore > 0) ((score.toFloat() / maxScore.toFloat()) * 100).toInt() else 0,
            modifier = Modifier.height(4.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
