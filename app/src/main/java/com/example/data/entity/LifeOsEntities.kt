package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val area: String, // LifeArea name
    val startDate: String = "",
    val targetDate: String = "",
    val priority: String = "HIGH", // Priority name
    val progressPercent: Int = 0, // 0 - 100
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, ON_HOLD
    val notes: String = ""
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val targetDate: String = ""
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long = 0, // 0 if standalone
    val title: String,
    val description: String = "",
    val area: String = "CAREER",
    val deadline: String = "",
    val priority: String = "HIGH",
    val progressPercent: Int = 0,
    val status: String = "IN_PROGRESS",
    val notes: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "HIGH", // LOW, MEDIUM, HIGH, URGENT
    val dueDate: String = "",
    val startDate: String = "",
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int = 0,
    val area: String = "CAREER",
    val projectId: Long = 0,
    val goalId: Long = 0,
    val tags: String = "", // Comma-separated
    val recurrence: String = "",
    val status: String = "TODAY", // INBOX, PLANNED, TODAY, IN_PROGRESS, COMPLETED, DEFERRED
    val subtasksJson: String = "", // serialized subtasks
    val notes: String = "",
    val priorityScore: Int = 50,
    val completedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "🔥",
    val frequency: String = "DAILY", // HabitFrequency
    val targetCount: Int = 1,
    val reminderTime: String = "08:00",
    val startDate: String = "",
    val goalId: Long = 0,
    val area: String = "HEALTH",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val streakFreezes: Int = 1
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val completedCount: Int = 1,
    val notes: String = ""
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long = 0,
    val taskTitle: String = "Deep Work",
    val area: String = "LEARNING",
    val durationMinutes: Int = 25,
    val targetMinutes: Int = 25,
    val completed: Boolean = true,
    val distractionCount: Int = 0,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // MORNING, NIGHT, CUSTOM
    val time: String,
    val stepsJson: String, // pipe or newline separated steps
    val isActive: Boolean = true
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val epochDay: Long,
    val mood: String = "GOOD",
    val gratitude: String = "",
    val wins: String = "",
    val improvements: String = "",
    val freeText: String = "",
    val tags: String = "",
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_memories")
data class AiMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "PREFERENCE",
    val updatedAt: Long = System.currentTimeMillis()
)
