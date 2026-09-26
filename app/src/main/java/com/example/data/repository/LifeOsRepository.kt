package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class LifeOsRepository(private val dao: LifeOsDao) {

    // --- Goals ---
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()
    fun getGoalsByArea(area: String): Flow<List<GoalEntity>> = dao.getGoalsByArea(area)
    suspend fun getGoalById(id: Long): GoalEntity? = dao.getGoalById(id)
    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun deleteGoal(id: Long) = dao.deleteGoal(id)

    // --- Milestones ---
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>> = dao.getMilestonesForGoal(goalId)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long = dao.insertMilestone(milestone)
    suspend fun updateMilestone(milestone: MilestoneEntity) = dao.updateMilestone(milestone)
    suspend fun deleteMilestone(id: Long) = dao.deleteMilestone(id)

    // --- Projects ---
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    fun getProjectsForGoal(goalId: Long): Flow<List<ProjectEntity>> = dao.getProjectsForGoal(goalId)
    suspend fun insertProject(project: ProjectEntity): Long = dao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = dao.updateProject(project)
    suspend fun deleteProject(id: Long) = dao.deleteProject(id)

    // --- Tasks ---
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val todayTasks: Flow<List<TaskEntity>> = dao.getTodayTasks()
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>> = dao.getTasksForProject(projectId)
    fun getTasksForGoal(goalId: Long): Flow<List<TaskEntity>> = dao.getTasksForGoal(goalId)
    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)

    suspend fun toggleTaskComplete(taskId: Long) {
        val task = dao.getTaskById(taskId) ?: return
        val newStatus = if (task.status == "COMPLETED") "TODAY" else "COMPLETED"
        val completedAt = if (newStatus == "COMPLETED") System.currentTimeMillis() else 0L
        dao.updateTask(task.copy(status = newStatus, completedAt = completedAt))
    }

    // --- Habits ---
    val allHabits: Flow<List<HabitEntity>> = dao.getAllHabits()
    fun getHabitLogsSince(fromEpochDay: Long): Flow<List<HabitLogEntity>> = dao.getHabitLogsSince(fromEpochDay)
    fun getHabitLogsForDate(epochDay: Long): Flow<List<HabitLogEntity>> = dao.getHabitLogsForDate(epochDay)
    suspend fun insertHabit(habit: HabitEntity): Long = dao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)
    suspend fun deleteHabit(id: Long) = dao.deleteHabit(id)

    suspend fun toggleHabitCheckIn(habitId: Long, epochDay: Long = LocalDate.now().toEpochDay()) {
        val habit = dao.getHabitById(habitId) ?: return
        val existingLog = dao.getHabitLogForDate(habitId, epochDay)

        if (existingLog != null) {
            dao.deleteHabitLogForDate(habitId, epochDay)
            val updatedStreak = maxOf(0, habit.currentStreak - 1)
            val updatedTotal = maxOf(0, habit.totalCompletions - 1)
            dao.updateHabit(habit.copy(currentStreak = updatedStreak, totalCompletions = updatedTotal))
        } else {
            dao.insertHabitLog(HabitLogEntity(habitId = habitId, dateEpochDay = epochDay))
            val updatedStreak = habit.currentStreak + 1
            val updatedLongest = maxOf(habit.longestStreak, updatedStreak)
            val updatedTotal = habit.totalCompletions + 1
            dao.updateHabit(habit.copy(
                currentStreak = updatedStreak,
                longestStreak = updatedLongest,
                totalCompletions = updatedTotal
            ))
        }
    }

    // --- Focus Sessions ---
    val allFocusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    fun getFocusSessionsSince(sinceEpochMs: Long): Flow<List<FocusSessionEntity>> = dao.getFocusSessionsSince(sinceEpochMs)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long = dao.insertFocusSession(session)

    // --- Routines ---
    val activeRoutines: Flow<List<RoutineEntity>> = dao.getActiveRoutines()
    suspend fun insertRoutine(routine: RoutineEntity): Long = dao.insertRoutine(routine)
    suspend fun updateRoutine(routine: RoutineEntity) = dao.updateRoutine(routine)
    suspend fun deleteRoutine(id: Long) = dao.deleteRoutine(id)

    // --- Journal ---
    val allJournalEntries: Flow<List<JournalEntryEntity>> = dao.getAllJournalEntries()
    suspend fun getJournalEntryForToday(): JournalEntryEntity? = dao.getJournalEntryForDay(LocalDate.now().toEpochDay())
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long = dao.insertJournalEntry(entry)
    suspend fun deleteJournalEntry(id: Long) = dao.deleteJournalEntry(id)

    // --- AI Memories ---
    val allAiMemories: Flow<List<AiMemoryEntity>> = dao.getAllAiMemories()
    suspend fun insertAiMemory(memory: AiMemoryEntity): Long = dao.insertAiMemory(memory)
    suspend fun deleteAiMemory(id: Long) = dao.deleteAiMemory(id)
}
