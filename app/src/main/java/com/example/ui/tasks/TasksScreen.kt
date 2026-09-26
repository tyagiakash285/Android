package com.example.ui.tasks

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.GoalEntity
import com.example.data.entity.TaskEntity
import com.example.data.model.LifeArea
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()

    var selectedTab by remember { mutableStateOf("TODAY") } // TODAY, UPCOMING, INBOX, COMPLETED
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(allTasks, selectedTab) {
        when (selectedTab) {
            "TODAY" -> allTasks.filter { (it.status == "TODAY" || it.status == "IN_PROGRESS") && it.status != "COMPLETED" }
            "UPCOMING" -> allTasks.filter { it.status == "PLANNED" }
            "INBOX" -> allTasks.filter { it.status == "INBOX" }
            "COMPLETED" -> allTasks.filter { it.status == "COMPLETED" }
            else -> allTasks
        }.sortedByDescending { it.priorityScore }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = LifeOsPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Status Tabs
            TabRow(
                selectedTabIndex = when (selectedTab) {
                    "TODAY" -> 0
                    "UPCOMING" -> 1
                    "INBOX" -> 2
                    "COMPLETED" -> 3
                    else -> 0
                },
                containerColor = Color.Transparent,
                contentColor = LifeOsPrimary
            ) {
                listOf(
                    Pair("Today", "TODAY"),
                    Pair("Upcoming", "UPCOMING"),
                    Pair("Inbox", "INBOX"),
                    Pair("Done", "COMPLETED")
                ).forEachIndexed { index, (label, key) ->
                    Tab(
                        selected = selectedTab == key,
                        onClick = { selectedTab = key },
                        text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explanation / Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTasks.size} Tasks",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Sorted by Smart Priority",
                    fontSize = 11.sp,
                    color = LifeOsPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✅", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks in this view",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + below to create a high-priority task",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onToggle = { viewModel.toggleTaskComplete(task.id) },
                            onStartFocus = { onStartFocus(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            goals = allGoals,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, priority, estMins, area, goalId, dueDate ->
                viewModel.createTask(
                    title = title,
                    description = desc,
                    priority = priority,
                    estimatedMinutes = estMins,
                    area = area,
                    goalId = goalId,
                    dueDate = dueDate
                )
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskListItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle task completion",
                        tint = if (isCompleted) LifeOsSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PriorityScoreBadge(score = task.priorityScore)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AreaBadge(areaName = task.area)
                    PriorityBadge(priorityName = task.priority)
                    Text(
                        text = "⏱ ${task.estimatedMinutes}m",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (task.dueDate.isNotBlank()) {
                        Text(
                            text = "📅 ${task.dueDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!isCompleted) {
                        IconButton(
                            onClick = onStartFocus,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Focus",
                                tint = DeepWorkPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    goals: List<GoalEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, priority: String, estMins: Int, area: String, goalId: Long, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var estMinsStr by remember { mutableStateOf("30") }
    var area by remember { mutableStateOf(LifeArea.CAREER.name) }
    var selectedGoalId by remember { mutableStateOf<Long>(goals.firstOrNull()?.id ?: 0L) }
    var dueDate by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Smart Task", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estMinsStr,
                        onValueChange = { estMinsStr = it },
                        label = { Text("Est. Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH", "URGENT").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 10.sp) }
                        )
                    }
                }

                Text("Life Area", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LifeArea.entries.toTypedArray()) { a ->
                        FilterChip(
                            selected = area == a.name,
                            onClick = { area = a.name },
                            label = { Text("${a.emoji} ${a.title}", fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = estMinsStr.toIntOrNull() ?: 30
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, priority, mins, area, selectedGoalId, dueDate)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
