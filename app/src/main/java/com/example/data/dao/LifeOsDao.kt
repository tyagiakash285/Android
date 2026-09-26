package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface LifeOsDao {

    // --- Goals ---
    @Query("SELECT * FROM goals ORDER BY progressPercent ASC, id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE area = :area")
    fun getGoalsByArea(area: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    // --- Milestones ---
    @Query("SELECT * FROM milestones WHERE goalId = :goalId")
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE goalId = :goalId")
    fun getProjectsForGoal(goalId: Long): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY status = 'COMPLETED', priorityScore DESC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'TODAY' OR status = 'IN_PROGRESS' ORDER BY priorityScore DESC")
    fun getTodayTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE goalId = :goalId")
    fun getTasksForGoal(goalId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY currentStreak DESC, id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs WHERE dateEpochDay >= :fromEpochDay")
    fun getHabitLogsSince(fromEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun getHabitLogForDate(habitId: Long, epochDay: Long): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay = :epochDay")
    fun getHabitLogsForDate(epochDay: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :epochDay")
    suspend fun deleteHabitLogForDate(habitId: Long, epochDay: Long)

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY timestampEpochMs DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestampEpochMs >= :sinceEpochMs ORDER BY timestampEpochMs DESC")
    fun getFocusSessionsSince(sinceEpochMs: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // --- Routines ---
    @Query("SELECT * FROM routines WHERE isActive = 1")
    fun getActiveRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)

    // --- Journal ---
    @Query("SELECT * FROM journal_entries ORDER BY epochDay DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getJournalEntryForDay(epochDay: Long): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: Long)

    // --- AI Memories ---
    @Query("SELECT * FROM ai_memories ORDER BY updatedAt DESC")
    fun getAllAiMemories(): Flow<List<AiMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMemory(memory: AiMemoryEntity): Long

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteAiMemory(id: Long)
}
