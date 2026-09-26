package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AgentRole
import com.example.data.entity.GoalEntity
import com.example.data.entity.TaskEntity
import com.example.ui.MainViewModel
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.coach.AiCoachScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.focus.FocusScreen
import com.example.ui.goals.GoalsScreen
import com.example.ui.habits.HabitsScreen
import com.example.ui.journal.JournalDialog
import com.example.ui.settings.SettingsDialog
import com.example.ui.tasks.TasksScreen
import com.example.ui.theme.LifeOsPrimary
import com.example.ui.theme.MyApplicationTheme

enum class LifeOsNavTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    TODAY("TODAY", "Today", Icons.Default.Home),
    GOALS("GOALS", "Goals", Icons.Default.TrackChanges),
    TASKS("TASKS", "Tasks", Icons.Default.CheckCircleOutline),
    HABITS("HABITS", "Habits", Icons.Default.LocalFireDepartment),
    FOCUS("FOCUS", "Focus", Icons.Default.Timer),
    ANALYTICS("ANALYTICS", "Analytics", Icons.Default.BarChart),
    COACH("COACH", "AI Coach", Icons.Default.SmartToy)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val selectedTab by viewModel.selectedTab.collectAsState()

                var showJournalDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                // Listen for Toast notifications
                LaunchedEffect(Unit) {
                    viewModel.userMessageToast.collect { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // Hardware back press handles navigating to TODAY if on other tab
                BackHandler(enabled = selectedTab != "TODAY") {
                    viewModel.selectTab("TODAY")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        LifeOsTopAppBar(
                            selectedTab = selectedTab,
                            onOpenJournal = { showJournalDialog = true },
                            onOpenSettings = { showSettingsDialog = true }
                        )
                    },
                    bottomBar = {
                        LifeOsBottomNav(
                            currentTab = selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_switch"
                        ) { targetTab ->
                            when (targetTab) {
                                "TODAY" -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToFocus = { task ->
                                        if (task != null) {
                                            viewModel.setTimerPreset(task.estimatedMinutes, task.title, task.area)
                                        }
                                        viewModel.selectTab("FOCUS")
                                    },
                                    onNavigateToTasks = { viewModel.selectTab("TASKS") },
                                    onNavigateToHabits = { viewModel.selectTab("HABITS") },
                                    onNavigateToCoach = { viewModel.selectTab("COACH") },
                                    onOpenJournal = { showJournalDialog = true }
                                )

                                "GOALS" -> GoalsScreen(
                                    viewModel = viewModel,
                                    onBreakdownGoalWithAi = { goal ->
                                        viewModel.selectAgentRole(AgentRole.GOAL_BREAKDOWN)
                                        viewModel.selectTab("COACH")
                                        viewModel.sendCoachMessage("Break down my goal: ${goal.title} in ${goal.area}")
                                    }
                                )

                                "TASKS" -> TasksScreen(
                                    viewModel = viewModel,
                                    onStartFocus = { task ->
                                        viewModel.setTimerPreset(task.estimatedMinutes, task.title, task.area)
                                        viewModel.selectTab("FOCUS")
                                    }
                                )

                                "HABITS" -> HabitsScreen(
                                    viewModel = viewModel
                                )

                                "FOCUS" -> FocusScreen(
                                    viewModel = viewModel
                                )

                                "ANALYTICS" -> AnalyticsScreen(
                                    viewModel = viewModel,
                                    onNavigateToCoach = {
                                        viewModel.selectAgentRole(AgentRole.WEEKLY_REVIEW)
                                        viewModel.selectTab("COACH")
                                        viewModel.sendCoachMessage("Generate a complete weekly productivity review.")
                                    }
                                )

                                "COACH" -> AiCoachScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                if (showJournalDialog) {
                    JournalDialog(
                        onDismiss = { showJournalDialog = false },
                        onSave = { mood, gratitude, wins, improvements, text ->
                            viewModel.saveJournal(mood, gratitude, wins, improvements, text)
                            showJournalDialog = false
                        }
                    )
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOsTopAppBar(
    selectedTab: String,
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(LifeOsPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LifeOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenJournal) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Journal & Reflection",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun LifeOsBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        LifeOsNavTab.entries.forEach { tab ->
            val isSelected = currentTab == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = LifeOsPrimary,
                    indicatorColor = LifeOsPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
