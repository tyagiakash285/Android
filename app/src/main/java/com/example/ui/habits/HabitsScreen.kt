package com.example.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.model.LifeArea
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()
    val todayLogs by viewModel.todayHabitLogs.collectAsState()
    val recentLogs by viewModel.recentHabitLogs.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val completedHabitIds = remember(todayLogs) {
        todayLogs.map { it.habitId }.toSet()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true },
                containerColor = LifeOsPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Streak Overview Header ---
            item {
                val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
                val totalCompletions = habits.sumOf { it.totalCompletions }
                val streakFreezes = habits.firstOrNull()?.streakFreezes ?: 1

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HABIT STREAK SYSTEM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = StreakOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Consistency Engine",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            StreakBadge(streakDays = maxStreak)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HabitStatItem(title = "Top Streak", value = "$maxStreak days", icon = "🔥")
                            HabitStatItem(title = "Completions", value = "$totalCompletions", icon = "✅")
                            HabitStatItem(title = "Streak Freezes", value = "$streakFreezes Available", icon = "🛡️")
                        }
                    }
                }
            }

            // --- 2. GitHub-Style Habit Calendar Heatmap ---
            item {
                SectionHeader(title = "HABIT CONSISTENCY CALENDAR", icon = "🟩")
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Last 4 Weeks Activity Grid",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        HabitContributionHeatmap(
                            recentLogs = recentLogs,
                            todayEpoch = todayEpoch
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Heatmap0))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Heatmap1))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Heatmap2))
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Heatmap3))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // --- 3. Habits List ---
            item {
                SectionHeader(title = "ACTIVE HABITS (${habits.size})", icon = "📋")
            }

            items(habits, key = { it.id }) { habit ->
                val isCompleted = completedHabitIds.contains(habit.id)
                HabitCardItem(
                    habit = habit,
                    isCompleted = isCompleted,
                    onToggle = { viewModel.toggleHabitCheckIn(habit.id) }
                )
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { name, icon, area, freq ->
                viewModel.createHabit(name, icon, area, freq)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
fun HabitStatItem(title: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HabitContributionHeatmap(
    recentLogs: List<HabitLogEntity>,
    todayEpoch: Long
) {
    // 4 weeks = 28 days grid: 7 rows (Mon-Sun), 4 columns (weeks)
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    // Map logs to counts per epochDay
    val logsByDay = remember(recentLogs) {
        recentLogs.groupBy { it.dateEpochDay }.mapValues { it.value.size }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day labels
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            dayNames.forEach { day ->
                Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = day, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 4 Columns of weeks
        for (week in 0..3) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dayIndex in 0..6) {
                    val daysAgo = (3 - week) * 7 + (6 - dayIndex)
                    val epoch = todayEpoch - daysAgo
                    val count = logsByDay[epoch] ?: 0

                    val cellColor = when {
                        count >= 4 -> Heatmap3
                        count >= 2 -> Heatmap2
                        count == 1 -> Heatmap1
                        else -> Heatmap0
                    }

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(cellColor)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitCardItem(
    habit: HabitEntity,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) LifeOsSecondary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCompleted) androidx.compose.foundation.BorderStroke(1.dp, LifeOsSecondary.copy(alpha = 0.4f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Habit",
                    tint = if (isCompleted) LifeOsSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AreaBadge(areaName = habit.area)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Best: ${habit.longestStreak}d • Total: ${habit.totalCompletions}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Streak indicator
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${habit.currentStreak}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StreakOrange
                    )
                }
                Text(
                    text = "day streak",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, area: String, frequency: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("⚡") }
    var area by remember { mutableStateOf(LifeArea.HEALTH.name) }
    var freq by remember { mutableStateOf("DAILY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Build New Habit", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name (e.g. Read 20 Pages)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Icon / Emoji", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🔥", "📖", "💪", "🧘", "💻", "💧", "☀️", "🎯").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (icon == emoji) LifeOsPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { icon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Text("Life Area", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LifeArea.entries.toTypedArray()) { a ->
                        FilterChip(
                            selected = area == a.name,
                            onClick = { area = a.name },
                            label = { Text("${a.emoji} ${a.title}", fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, icon, area, freq) },
                enabled = name.isNotBlank()
            ) {
                Text("Create Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
