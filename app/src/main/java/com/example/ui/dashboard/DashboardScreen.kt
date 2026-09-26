package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.data.entity.TaskEntity
import com.example.domain.ProductivityBreakdown
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToFocus: (TaskEntity?) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToCoach: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val todayLogs by viewModel.todayHabitLogs.collectAsState()
    val breakdown by viewModel.productivityBreakdown.collectAsState()
    val focusSessions by viewModel.allFocusSessions.collectAsState()
    val routines by viewModel.activeRoutines.collectAsState()

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    }

    val totalFocusMinsToday = breakdown.focusMinutesToday
    val totalFocusMinsWeek = remember(focusSessions) {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 3600 * 1000L)
        focusSessions.filter { it.timestampEpochMs >= weekAgo }.sumOf { it.durationMinutes }
    }

    val completedHabitIds = remember(todayLogs) {
        todayLogs.map { it.habitId }.toSet()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Greeting & Streak ---
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
                                    Color(0xFF38BDF8).copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Good Morning 👋",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = todayFormatted,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            StreakBadge(streakDays = 12)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Productivity Score bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "TODAY'S PRODUCTIVITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${breakdown.totalScore}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LifeOsPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        ProgressBarCustom(
                            progressPercent = breakdown.totalScore,
                            color = if (breakdown.totalScore >= 75) LifeOsSecondary else LifeOsPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasks: ${breakdown.tasksCompletedCount}/${breakdown.tasksPlannedCount}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Habits: ${breakdown.habitsCompletedToday}/${breakdown.totalHabitsCount}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Focus: ${totalFocusMinsToday / 60}h ${totalFocusMinsToday % 60}m",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 2. AI Coach Quick Recommendation ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LifeOsPrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LifeOsPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🤖", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI COACH INSIGHT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LifeOsPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Real-time",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"You have ${tasks.count { it.status != "COMPLETED" }} high-priority tasks remaining. Start with ${tasks.firstOrNull()?.title ?: "Deep Work"} because you perform better when momentum starts early.\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateToFocus(tasks.firstOrNull()) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LifeOsPrimary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToCoach,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask Coach", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 3. Top Priorities (Smart Priority Sort) ---
        item {
            SectionHeader(
                title = "TOP PRIORITIES",
                icon = "🎯",
                actionText = "View All (${tasks.size})",
                onActionClick = onNavigateToTasks
            )
        }

        val topTasks = tasks.sortedByDescending { it.priorityScore }.take(4)
        if (topTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks scheduled for today. Tap + to add one!", fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(topTasks, key = { it.id }) { task ->
                DashboardTaskItem(
                    task = task,
                    onToggle = { viewModel.toggleTaskComplete(task.id) },
                    onFocus = { onNavigateToFocus(task) }
                )
            }
        }

        // --- 4. Habits Check-in ---
        item {
            SectionHeader(
                title = "HABITS",
                icon = "🔥",
                actionText = "Streaks",
                onActionClick = onNavigateToHabits
            )
        }

        val displayHabits = habits.take(5)
        items(displayHabits, key = { it.id }) { habit ->
            val isCompleted = completedHabitIds.contains(habit.id)
            DashboardHabitItem(
                habit = habit,
                isCompleted = isCompleted,
                onToggle = { viewModel.toggleHabitCheckIn(habit.id) }
            )
        }

        // --- 5. Focus Widget ---
        item {
            SectionHeader(title = "FOCUS", icon = "⏱️")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today: ${totalFocusMinsToday / 60}h ${totalFocusMinsToday % 60}m",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "This week: ${totalFocusMinsWeek / 60}h ${totalFocusMinsWeek % 60}m",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { onNavigateToFocus(null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DeepWorkPurple.copy(alpha = 0.15f),
                                contentColor = DeepWorkPurple
                            )
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Timer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 6. Daily Routine Quick Peek ---
        if (routines.isNotEmpty()) {
            item {
                SectionHeader(title = "DAILY ROUTINES", icon = "☀️")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routines.forEach { routine ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (routine.type == "MORNING") "🌅" else "🌙",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = routine.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${routine.time} • ${routine.stepsJson.lines().size} steps",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LifeOsSecondary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Quick Reflection CTA ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenJournal() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📔", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Reflection & Journal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Log wins, gratitude, and tomorrow's improvements",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Reflect",
                        tint = LifeOsPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTaskItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onFocus: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (isCompleted) LifeOsSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AreaBadge(areaName = task.area)
                    Text(
                        text = "${task.estimatedMinutes}m",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PriorityScoreBadge(score = task.priorityScore)

            if (!isCompleted) {
                IconButton(
                    onClick = onFocus,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Start Focus",
                        tint = DeepWorkPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHabitItem(
    habit: HabitEntity,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) LifeOsSecondary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCompleted) androidx.compose.foundation.BorderStroke(1.dp, LifeOsSecondary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Check habit",
                    tint = if (isCompleted) LifeOsSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = habit.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔥", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${habit.currentStreak}d",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StreakOrange
                )
            }
        }
    }
}
