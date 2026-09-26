package com.example.ui.goals

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.GoalEntity
import com.example.data.entity.ProjectEntity
import com.example.data.model.LifeArea
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.LifeOsPrimary
import com.example.ui.theme.LifeOsSecondary

@Composable
fun GoalsScreen(
    viewModel: MainViewModel,
    onBreakdownGoalWithAi: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsState()
    val projects by viewModel.allProjects.collectAsState()

    var selectedAreaFilter by remember { mutableStateOf<String?>("ALL") }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(goals, selectedAreaFilter) {
        if (selectedAreaFilter == null || selectedAreaFilter == "ALL") goals
        else goals.filter { it.area.equals(selectedAreaFilter, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = LifeOsPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
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
            // --- Vision Banner ---
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
                                        Color(0xFF6366F1).copy(alpha = 0.15f),
                                        Color(0xFFA855F7).copy(alpha = 0.10f)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🔭", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VISION ROADMAP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = LifeOsPrimary
                                    )
                                }
                                Text(
                                    text = "${goals.size} Active Goals",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "“Turn long-term vision into daily execution. Break ambitions into projects, projects into tasks, and tasks into habits.”",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // --- Area Filters ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedAreaFilter == "ALL",
                            onClick = { selectedAreaFilter = "ALL" },
                            label = { Text("All Areas", fontSize = 12.sp) }
                        )
                    }
                    items(LifeArea.entries.toTypedArray()) { area ->
                        FilterChip(
                            selected = selectedAreaFilter == area.name,
                            onClick = { selectedAreaFilter = area.name },
                            label = { Text("${area.emoji} ${area.title}", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // --- Goals List ---
            item {
                SectionHeader(title = "GOALS & MILESTONES", icon = "🎯")
            }

            if (filteredGoals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No goals found for this area. Tap + to set a new goal!", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        projects = projects.filter { it.goalId == goal.id },
                        onUpdateProgress = { newProgress -> viewModel.updateGoalProgress(goal, newProgress) },
                        onBreakdownWithAi = { onBreakdownGoalWithAi(goal) }
                    )
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, desc, area, targetDate, priority ->
                viewModel.createGoal(title, desc, area, targetDate, priority)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    projects: List<ProjectEntity>,
    onUpdateProgress: (Int) -> Unit,
    onBreakdownWithAi: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AreaBadge(areaName = goal.area)
                PriorityBadge(priorityName = goal.priority)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = goal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${goal.progressPercent}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (goal.progressPercent >= 70) LifeOsSecondary else LifeOsPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBarCustom(
                progressPercent = goal.progressPercent,
                color = if (goal.progressPercent >= 70) LifeOsSecondary else LifeOsPrimary
            )

            // Slider to adjust progress
            Slider(
                value = goal.progressPercent.toFloat(),
                onValueChange = { onUpdateProgress(it.toInt()) },
                valueRange = 0f..100f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = LifeOsPrimary,
                    activeTrackColor = LifeOsPrimary
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            if (goal.targetDate.isNotBlank()) {
                Text(
                    text = "Target: ${goal.targetDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Linked Projects
            if (projects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PROJECTS (${projects.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                projects.forEach { proj ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${proj.title}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${proj.progressPercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LifeOsPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // AI Breakdown Button
            OutlinedButton(
                onClick = onBreakdownWithAi,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(text = "🤖", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Breakdown into Projects & Tasks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, area: String, targetDate: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf(LifeArea.CAREER.name) }
    var targetDate by remember { mutableStateOf("2026-12-31") }
    var priority by remember { mutableStateOf("HIGH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set New Long-Term Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Become AI Engineer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Motivation") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Life Area", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LifeArea.entries.toTypedArray()) { area ->
                        FilterChip(
                            selected = selectedArea == area.name,
                            onClick = { selectedArea = area.name },
                            label = { Text("${area.emoji} ${area.title}", fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Completion Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, desc, selectedArea, targetDate, priority) },
                enabled = title.isNotBlank()
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
