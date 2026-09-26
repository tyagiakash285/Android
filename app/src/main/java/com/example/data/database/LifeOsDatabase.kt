package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LifeOsDao
import com.example.data.entity.AiMemoryEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.HabitLogEntity
import com.example.data.entity.JournalEntryEntity
import com.example.data.entity.MilestoneEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.RoutineEntity
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        GoalEntity::class,
        MilestoneEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        FocusSessionEntity::class,
        RoutineEntity::class,
        JournalEntryEntity::class,
        AiMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LifeOsDatabase : RoomDatabase() {
    abstract fun lifeOsDao(): LifeOsDao

    companion object {
        @Volatile
        private var INSTANCE: LifeOsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LifeOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeOsDatabase::class.java,
                    "lifeos_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.lifeOsDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: LifeOsDao) {
            val todayEpochDay = LocalDate.now().toEpochDay()
            val nowMs = System.currentTimeMillis()

            // 1. Goals
            val goalId1 = dao.insertGoal(
                GoalEntity(
                    title = "Master Agentic AI & Systems",
                    description = "Build autonomous agents, multi-agent frameworks, and deploy production AI products.",
                    area = "CAREER",
                    startDate = LocalDate.now().minusMonths(2).toString(),
                    targetDate = LocalDate.now().plusMonths(3).toString(),
                    priority = "URGENT",
                    progressPercent = 65,
                    status = "ACTIVE",
                    notes = "Core driver for career elevation and AI engineering mastery."
                )
            )

            val goalId2 = dao.insertGoal(
                GoalEntity(
                    title = "Peak Athletic Fitness & Strength",
                    description = "Achieve sub-14% body fat, 5km under 24m, and strong mobility.",
                    area = "HEALTH",
                    startDate = LocalDate.now().minusMonths(1).toString(),
                    targetDate = LocalDate.now().plusMonths(5).toString(),
                    priority = "HIGH",
                    progressPercent = 50,
                    status = "ACTIVE",
                    notes = "Energy foundation for high cognitive performance."
                )
            )

            val goalId3 = dao.insertGoal(
                GoalEntity(
                    title = "Read 24 Non-Fiction Books",
                    description = "Deep study of mental models, distributed systems, and psychology.",
                    area = "KNOWLEDGE",
                    startDate = LocalDate.now().minusMonths(3).toString(),
                    targetDate = LocalDate.now().plusMonths(6).toString(),
                    priority = "MEDIUM",
                    progressPercent = 40,
                    status = "ACTIVE",
                    notes = "Compound knowledge reading habit."
                )
            )

            // Milestones for Goal 1
            dao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Python Advanced Patterns", isCompleted = true))
            dao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "OpenAI & Gemini API Mastery", isCompleted = true))
            dao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Advanced RAG & Vector Stores", isCompleted = true))
            dao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Multi-Agent System & Tool Calling", isCompleted = false))
            dao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Production Deployment & Monitoring", isCompleted = false))

            // 2. Projects
            val proj1 = dao.insertProject(
                ProjectEntity(
                    goalId = goalId1,
                    title = "Autonomous Research Agent",
                    description = "Full-stack agent that crawls docs, synthesizes findings, and creates executive briefs.",
                    area = "CAREER",
                    deadline = LocalDate.now().plusWeeks(2).toString(),
                    priority = "URGENT",
                    progressPercent = 70,
                    status = "IN_PROGRESS"
                )
            )

            val proj2 = dao.insertProject(
                ProjectEntity(
                    goalId = goalId1,
                    title = "Multi-Agent Workflow Orchestrator",
                    description = "Supervisor-worker agent system with dynamic tool selection.",
                    area = "CAREER",
                    deadline = LocalDate.now().plusWeeks(5).toString(),
                    priority = "HIGH",
                    progressPercent = 30,
                    status = "IN_PROGRESS"
                )
            )

            // 3. Tasks
            dao.insertTask(
                TaskEntity(
                    title = "Complete project documentation",
                    description = "Finalize API contracts and architecture diagrams for the research agent.",
                    priority = "URGENT",
                    dueDate = LocalDate.now().toString(),
                    estimatedMinutes = 45,
                    area = "CAREER",
                    projectId = proj1,
                    goalId = goalId1,
                    tags = "Documentation, Engineering",
                    status = "TODAY",
                    priorityScore = 95
                )
            )

            dao.insertTask(
                TaskEntity(
                    title = "Python practice — 60 min",
                    description = "Implement custom tool-calling parser and retry strategy in Python.",
                    priority = "HIGH",
                    dueDate = LocalDate.now().toString(),
                    estimatedMinutes = 60,
                    area = "LEARNING",
                    projectId = proj1,
                    goalId = goalId1,
                    tags = "Coding, DeepWork",
                    status = "TODAY",
                    priorityScore = 87
                )
            )

            dao.insertTask(
                TaskEntity(
                    title = "Gym workout — Push Day",
                    description = "Bench press, incline dumbbell press, shoulder accessory circuit.",
                    priority = "HIGH",
                    dueDate = LocalDate.now().toString(),
                    estimatedMinutes = 60,
                    area = "HEALTH",
                    goalId = goalId2,
                    tags = "Fitness, Health",
                    status = "TODAY",
                    priorityScore = 75
                )
            )

            dao.insertTask(
                TaskEntity(
                    title = "Read — 20 min (Designing Data-Intensive Apps)",
                    description = "Chapter 7: Transactions and isolation levels.",
                    priority = "MEDIUM",
                    dueDate = LocalDate.now().toString(),
                    estimatedMinutes = 20,
                    area = "KNOWLEDGE",
                    goalId = goalId3,
                    tags = "Reading, Focus",
                    status = "TODAY",
                    priorityScore = 60
                )
            )

            dao.insertTask(
                TaskEntity(
                    title = "Setup Chroma vector database",
                    description = "Test embeddings retrieval latency.",
                    priority = "MEDIUM",
                    dueDate = LocalDate.now().plusDays(2).toString(),
                    estimatedMinutes = 40,
                    area = "CAREER",
                    projectId = proj2,
                    goalId = goalId1,
                    tags = "Backend",
                    status = "PLANNED",
                    priorityScore = 55
                )
            )

            // 4. Habits
            val h1 = dao.insertHabit(
                HabitEntity(
                    name = "Wake up at 06:30 AM",
                    icon = "☀️",
                    frequency = "DAILY",
                    targetCount = 1,
                    area = "HEALTH",
                    currentStreak = 12,
                    longestStreak = 24,
                    totalCompletions = 45,
                    streakFreezes = 2
                )
            )

            val h2 = dao.insertHabit(
                HabitEntity(
                    name = "Daily Exercise / Gym",
                    icon = "💪",
                    frequency = "DAILY",
                    targetCount = 1,
                    area = "HEALTH",
                    goalId = goalId2,
                    currentStreak = 12,
                    longestStreak = 18,
                    totalCompletions = 38,
                    streakFreezes = 1
                )
            )

            val h3 = dao.insertHabit(
                HabitEntity(
                    name = "Python & AI Practice",
                    icon = "💻",
                    frequency = "DAILY",
                    targetCount = 1,
                    area = "LEARNING",
                    goalId = goalId1,
                    currentStreak = 8,
                    longestStreak = 15,
                    totalCompletions = 32,
                    streakFreezes = 1
                )
            )

            val h4 = dao.insertHabit(
                HabitEntity(
                    name = "Mindful Meditation",
                    icon = "🧘",
                    frequency = "DAILY",
                    targetCount = 1,
                    area = "HEALTH",
                    currentStreak = 5,
                    longestStreak = 10,
                    totalCompletions = 22,
                    streakFreezes = 1
                )
            )

            val h5 = dao.insertHabit(
                HabitEntity(
                    name = "Evening Reading (20m)",
                    icon = "📖",
                    frequency = "DAILY",
                    targetCount = 1,
                    area = "KNOWLEDGE",
                    goalId = goalId3,
                    currentStreak = 3,
                    longestStreak = 14,
                    totalCompletions = 29,
                    streakFreezes = 1
                )
            )

            // Habit logs for heatmap (last 21 days)
            for (dayOffset in 0..20) {
                val epoch = todayEpochDay - dayOffset
                // Seed realistic completion pattern
                if (dayOffset != 3 && dayOffset != 11) {
                    dao.insertHabitLog(HabitLogEntity(habitId = h1, dateEpochDay = epoch))
                }
                if (dayOffset != 5 && dayOffset != 14) {
                    dao.insertHabitLog(HabitLogEntity(habitId = h2, dateEpochDay = epoch))
                }
                if (dayOffset < 8) {
                    dao.insertHabitLog(HabitLogEntity(habitId = h3, dateEpochDay = epoch))
                }
            }

            // 5. Focus Sessions (seed today's 2h 35m + weekly sessions)
            dao.insertFocusSession(
                FocusSessionEntity(
                    taskTitle = "AI Agent Tool Calling",
                    area = "LEARNING",
                    durationMinutes = 50,
                    targetMinutes = 50,
                    completed = true,
                    timestampEpochMs = nowMs - (3 * 3600 * 1000L)
                )
            )
            dao.insertFocusSession(
                FocusSessionEntity(
                    taskTitle = "Architecture Design Docs",
                    area = "CAREER",
                    durationMinutes = 55,
                    targetMinutes = 50,
                    completed = true,
                    timestampEpochMs = nowMs - (2 * 3600 * 1000L)
                )
            )
            dao.insertFocusSession(
                FocusSessionEntity(
                    taskTitle = "Prompt Engineering",
                    area = "CAREER",
                    durationMinutes = 50,
                    targetMinutes = 50,
                    completed = true,
                    timestampEpochMs = nowMs - (1 * 3600 * 1000L)
                )
            )

            // 6. Routines
            dao.insertRoutine(
                RoutineEntity(
                    name = "Morning Launchpad",
                    type = "MORNING",
                    time = "06:30",
                    stepsJson = "06:30 Wake up & Hydrate (500ml)\n06:40 10m Mindfulness Meditation\n07:00 45m High-Energy Exercise\n07:45 High-Protein Breakfast\n08:00 Review LifeOS Top Priorities"
                )
            )

            dao.insertRoutine(
                RoutineEntity(
                    name = "Evening Wind-Down",
                    type = "NIGHT",
                    time = "21:30",
                    stepsJson = "21:30 Close work apps & Clean desk\n21:40 Daily Reflection & Journaling in LifeOS\n22:00 Read 20m physical book\n22:30 Phone to sleep mode\n23:00 Lights out"
                )
            )

            // 7. Reflection / Journal
            dao.insertJournalEntry(
                JournalEntryEntity(
                    dateString = LocalDate.now().minusDays(1).toString(),
                    epochDay = todayEpochDay - 1,
                    mood = "GREAT",
                    gratitude = "Grateful for deep focus, good health, and family support.",
                    wins = "Delivered the agent prototype and hit 12-day streak!",
                    improvements = "Protect morning reading from Slack interruptions.",
                    freeText = "Solid execution day. Maintained composure during debugging."
                )
            )

            // 8. AI Memory
            dao.insertAiMemory(
                AiMemoryEntity(
                    key = "primary_focus",
                    value = "Become proficient in Agentic AI and master multi-agent workflows.",
                    category = "GOALS"
                )
            )
            dao.insertAiMemory(
                AiMemoryEntity(
                    key = "peak_energy_window",
                    value = "Performs deep programming best in the evening (7pm - 10pm).",
                    category = "PRODUCTIVITY_PATTERN"
                )
            )
        }
    }
}
