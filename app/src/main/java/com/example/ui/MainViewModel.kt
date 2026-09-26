package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AgentAction
import com.example.ai.AgentRole
import com.example.ai.AiCoachEngine
import com.example.ai.CoachMessage
import com.example.data.database.LifeOsDatabase
import com.example.data.entity.*
import com.example.data.repository.LifeOsRepository
import com.example.domain.PriorityEngine
import com.example.domain.ProductivityBreakdown
import com.example.domain.ProductivityEngine
import com.example.domain.ProductivityPattern
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LifeOsDatabase.getDatabase(application, viewModelScope)
    private val repository = LifeOsRepository(database.lifeOsDao())

    // --- Database Flows ---
    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<TaskEntity>> = repository.todayTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayEpochDay = LocalDate.now().toEpochDay()
    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getHabitLogsForDate(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getHabitLogsSince(todayEpochDay - 28)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRoutines: StateFlow<List<RoutineEntity>> = repository.activeRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Productivity Score & Patterns ---
    val productivityBreakdown: StateFlow<ProductivityBreakdown> = combine(
        todayTasks,
        allHabits,
        todayHabitLogs,
        allFocusSessions,
        allGoals
    ) { tasks, habits, logs, focus, goals ->
        val todayFocus = focus.filter {
            val sessionDate = LocalDate.ofEpochDay(it.timestampEpochMs / (1000 * 60 * 60 * 24))
            sessionDate == LocalDate.now()
        }
        ProductivityEngine.calculateProductivity(tasks, habits, logs, todayFocus, goals)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProductivityEngine.calculateProductivity(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )

    val patterns: StateFlow<List<ProductivityPattern>> = combine(
        allTasks,
        allHabits,
        allFocusSessions
    ) { tasks, habits, focus ->
        ProductivityEngine.detectPatterns(tasks, habits, focus)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Focus Timer State ---
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _timerSecondsRemaining = MutableStateFlow(25 * 60)
    val timerSecondsRemaining = _timerSecondsRemaining.asStateFlow()

    private val _timerInitialSeconds = MutableStateFlow(25 * 60)
    val timerInitialSeconds = _timerInitialSeconds.asStateFlow()

    private val _activeFocusTask = MutableStateFlow("Deep Work Session")
    val activeFocusTask = _activeFocusTask.asStateFlow()

    private val _activeFocusArea = MutableStateFlow("LEARNING")
    val activeFocusArea = _activeFocusArea.asStateFlow()

    private val _distractionCount = MutableStateFlow(0)
    val distractionCount = _distractionCount.asStateFlow()

    private var timerJob: Job? = null

    // --- AI Coach Chat State ---
    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                sender = "AI",
                text = "Welcome to LifeOS! I am your AI Personal Coach.\n\nI monitor your goals, tasks, habits, and focus hours in real time. Ask me to plan your day, break down a new goal, analyze your habits, or review your weekly output.",
                agentRole = AgentRole.GENERAL
            )
        )
    )
    val coachMessages = _coachMessages.asStateFlow()

    private val _isCoachLoading = MutableStateFlow(false)
    val isCoachLoading = _isCoachLoading.asStateFlow()

    private val _selectedAgentRole = MutableStateFlow(AgentRole.GENERAL)
    val selectedAgentRole = _selectedAgentRole.asStateFlow()

    // --- UI Navigation & Dialog State ---
    private val _selectedTab = MutableStateFlow("TODAY") // TODAY, GOALS, TASKS, HABITS, FOCUS, ANALYTICS, COACH
    val selectedTab = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _userMessageToast = MutableSharedFlow<String>()
    val userMessageToast = _userMessageToast.asSharedFlow()

    // --- Actions ---

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectAgentRole(role: AgentRole) {
        _selectedAgentRole.value = role
    }

    fun toggleTaskComplete(taskId: Long) {
        viewModelScope.launch {
            repository.toggleTaskComplete(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun toggleHabitCheckIn(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitCheckIn(habitId)
        }
    }

    fun createTask(
        title: String,
        description: String = "",
        priority: String = "HIGH",
        dueDate: String = LocalDate.now().toString(),
        estimatedMinutes: Int = 30,
        area: String = "CAREER",
        projectId: Long = 0,
        goalId: Long = 0,
        tags: String = ""
    ) {
        viewModelScope.launch {
            val score = PriorityEngine.calculateScore(
                priority = priority,
                dueDateStr = dueDate,
                goalId = goalId,
                projectId = projectId,
                estimatedMinutes = estimatedMinutes
            )
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    estimatedMinutes = estimatedMinutes,
                    area = area,
                    projectId = projectId,
                    goalId = goalId,
                    tags = tags,
                    status = "TODAY",
                    priorityScore = score
                )
            )
            _userMessageToast.emit("Task '$title' created with priority score $score")
        }
    }

    fun createGoal(
        title: String,
        description: String,
        area: String,
        targetDate: String,
        priority: String
    ) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    title = title,
                    description = description,
                    area = area,
                    startDate = LocalDate.now().toString(),
                    targetDate = targetDate,
                    priority = priority,
                    progressPercent = 0,
                    status = "ACTIVE"
                )
            )
            _userMessageToast.emit("Goal '$title' added to your Vision roadmap")
        }
    }

    fun updateGoalProgress(goal: GoalEntity, newProgress: Int) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(progressPercent = newProgress.coerceIn(0, 100)))
        }
    }

    fun createHabit(
        name: String,
        icon: String = "🔥",
        area: String = "HEALTH",
        frequency: String = "DAILY",
        targetCount: Int = 1
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name,
                    icon = icon,
                    area = area,
                    frequency = frequency,
                    targetCount = targetCount,
                    startDate = LocalDate.now().toString(),
                    currentStreak = 0,
                    longestStreak = 0,
                    totalCompletions = 0,
                    streakFreezes = 1
                )
            )
            _userMessageToast.emit("Habit '$name' created")
        }
    }

    fun createProject(
        title: String,
        area: String = "CAREER",
        goalId: Long = 0,
        deadline: String = LocalDate.now().plusWeeks(2).toString()
    ) {
        viewModelScope.launch {
            repository.insertProject(
                ProjectEntity(
                    title = title,
                    area = area,
                    goalId = goalId,
                    deadline = deadline,
                    priority = "HIGH",
                    progressPercent = 0,
                    status = "IN_PROGRESS"
                )
            )
            _userMessageToast.emit("Project '$title' created")
        }
    }

    fun saveJournal(
        mood: String,
        gratitude: String,
        wins: String,
        improvements: String,
        text: String
    ) {
        viewModelScope.launch {
            repository.insertJournalEntry(
                JournalEntryEntity(
                    dateString = LocalDate.now().toString(),
                    epochDay = LocalDate.now().toEpochDay(),
                    mood = mood,
                    gratitude = gratitude,
                    wins = wins,
                    improvements = improvements,
                    freeText = text
                )
            )
            _userMessageToast.emit("Daily Reflection saved. Excellent work today!")
        }
    }

    // --- Focus Timer Controls ---

    fun setTimerPreset(minutes: Int, taskTitle: String = "Deep Work Session", area: String = "LEARNING") {
        pauseFocusTimer()
        _timerInitialSeconds.value = minutes * 60
        _timerSecondsRemaining.value = minutes * 60
        _activeFocusTask.value = taskTitle
        _activeFocusArea.value = area
    }

    fun startFocusTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerSecondsRemaining.value -= 1
            }
            if (_timerSecondsRemaining.value <= 0) {
                _isTimerRunning.value = false
                val elapsedMinutes = _timerInitialSeconds.value / 60
                repository.insertFocusSession(
                    FocusSessionEntity(
                        taskTitle = _activeFocusTask.value,
                        area = _activeFocusArea.value,
                        durationMinutes = elapsedMinutes,
                        targetMinutes = elapsedMinutes,
                        completed = true,
                        distractionCount = _distractionCount.value
                    )
                )
                _userMessageToast.emit("🎉 Focus Session Completed ($elapsedMinutes min)! Deep work logged.")
                _distractionCount.value = 0
            }
        }
    }

    fun pauseFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        _timerSecondsRemaining.value = _timerInitialSeconds.value
    }

    fun recordDistraction() {
        _distractionCount.value += 1
    }

    // --- AI Coach Chat ---

    fun sendCoachMessage(prompt: String) {
        if (prompt.isBlank()) return
        val currentRole = _selectedAgentRole.value

        // Append user message immediately
        val userMsg = CoachMessage(
            sender = "USER",
            text = prompt,
            agentRole = currentRole
        )
        _coachMessages.value = _coachMessages.value + userMsg
        _isCoachLoading.value = true

        viewModelScope.launch {
            try {
                val aiResponse = AiCoachEngine.generateResponse(
                    userPrompt = prompt,
                    role = currentRole,
                    todayTasks = allTasks.value,
                    habits = allHabits.value,
                    goals = allGoals.value,
                    focusSessions = allFocusSessions.value
                )
                _coachMessages.value = _coachMessages.value + aiResponse
            } catch (e: Exception) {
                _coachMessages.value = _coachMessages.value + CoachMessage(
                    sender = "AI",
                    text = "I encountered an issue analyzing your request: ${e.message}",
                    agentRole = currentRole
                )
            } finally {
                _isCoachLoading.value = false
            }
        }
    }

    fun executeAgentAction(action: AgentAction) {
        when (action) {
            is AgentAction.CreateTask -> {
                createTask(
                    title = action.title,
                    estimatedMinutes = action.estimatedMins,
                    priority = action.priority,
                    area = action.area
                )
            }
            is AgentAction.CreateHabit -> {
                createHabit(
                    name = action.name,
                    icon = action.icon,
                    area = action.area,
                    frequency = action.frequency
                )
            }
            is AgentAction.CreateProject -> {
                createProject(
                    title = action.title,
                    area = action.area
                )
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            LifeOsDatabase.populateInitialData(database.lifeOsDao())
            _userMessageToast.emit("Sample LifeOS data populated successfully!")
        }
    }
}
