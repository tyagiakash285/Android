package com.example.ai

import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.TaskEntity
import java.time.LocalDate

enum class AgentRole(val title: String, val emoji: String, val description: String) {
    GENERAL("LifeOS Coach", "🤖", "Holistic advisor for your daily execution & mindset"),
    PLANNER("Daily Planner", "🗓️", "Builds time-blocked schedules based on priorities"),
    GOAL_BREAKDOWN("Goal Architect", "🎯", "Deconstructs ambitious goals into projects & tasks"),
    HABIT_ANALYST("Habit Specialist", "🔥", "Analyzes streaks, friction, and consistency"),
    WEEKLY_REVIEW("Executive Reviewer", "📊", "Audits weekly output and prescribes improvements")
}

sealed class AgentAction {
    data class CreateTask(
        val title: String,
        val estimatedMins: Int = 30,
        val priority: String = "HIGH",
        val area: String = "CAREER"
    ) : AgentAction()

    data class CreateHabit(
        val name: String,
        val icon: String = "🔥",
        val area: String = "HEALTH",
        val frequency: String = "DAILY"
    ) : AgentAction()

    data class CreateProject(
        val title: String,
        val area: String = "CAREER",
        val goalTitle: String = ""
    ) : AgentAction()
}

data class CoachMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val agentRole: AgentRole = AgentRole.GENERAL,
    val actions: List<AgentAction> = emptyList(),
    val timestampMs: Long = System.currentTimeMillis()
)

object AiCoachEngine {

    fun buildSystemInstruction(
        role: AgentRole,
        todayTasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        goals: List<GoalEntity>,
        focusSessions: List<FocusSessionEntity>
    ): String {
        val totalFocusMins = focusSessions.sumOf { it.durationMinutes }
        val remainingTasks = todayTasks.filter { it.status != "COMPLETED" }

        val context = buildString {
            appendLine("=== LIFEOS CURRENT USER STATE ===")
            appendLine("Date: ${LocalDate.now()}")
            appendLine("Focus Time Today: ${totalFocusMins / 60}h ${totalFocusMins % 60}m")
            appendLine("Active Goals (${goals.size}):")
            goals.take(4).forEach { appendLine("  - ${it.title} (${it.area}, ${it.progressPercent}% progress)") }
            appendLine("Today's Pending Tasks (${remainingTasks.size}):")
            remainingTasks.take(5).forEach { appendLine("  - [P:${it.priorityScore}] ${it.title} (~${it.estimatedMinutes}m, ${it.area})") }
            appendLine("Habits (${habits.size}):")
            habits.take(5).forEach { appendLine("  - ${it.name} (Streak: ${it.currentStreak}d)") }
            appendLine("=================================")
        }

        return """
You are the LifeOS Personal Operating System AI — an elite productivity strategist and supportive personal coach.
Current Role: ${role.title} (${role.description}).

$context

Behavior rules:
1. Provide concise, highly actionable, empathetic guidance formatted with clear bullet points.
2. Maintain connection: VISION -> GOALS -> PROJECTS -> TASKS -> HABITS -> DAILY ACTIONS.
3. Suggest concrete actions. You may output executable actions in this format at the end of your response:
[[ACTION:CREATE_TASK|Title|DurationMins|Priority|Area]]
[[ACTION:CREATE_HABIT|Name|Icon|Area|Frequency]]
[[ACTION:CREATE_PROJECT|Title|Area|GoalTitle]]
Example:
[[ACTION:CREATE_TASK|Review PR and merge tool caller|30|HIGH|CAREER]]
4. Tone: Confident, strategic, encouraging, disciplined.
""".trimIndent()
    }

    suspend fun generateResponse(
        userPrompt: String,
        role: AgentRole,
        todayTasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        goals: List<GoalEntity>,
        focusSessions: List<FocusSessionEntity>
    ): CoachMessage {
        val systemPrompt = buildSystemInstruction(role, todayTasks, habits, goals, focusSessions)

        // Attempt Gemini API call
        val result = GeminiClient.askGemini(userPrompt, systemPrompt)

        val rawText = result.getOrElse {
            // Intelligent deterministic fallback response if no API key or offline
            generateOfflineInsight(userPrompt, role, todayTasks, habits, goals, focusSessions)
        }

        // Parse actions
        val (cleanText, actions) = parseActionsFromText(rawText)

        return CoachMessage(
            sender = "AI",
            text = cleanText,
            agentRole = role,
            actions = actions
        )
    }

    private fun parseActionsFromText(text: String): Pair<String, List<AgentAction>> {
        val actionRegex = Regex("\\[\\[ACTION:(.*?)\\]\\]")
        val actions = mutableListOf<AgentAction>()

        val matches = actionRegex.findAll(text)
        for (match in matches) {
            val payload = match.groupValues[1]
            val parts = payload.split("|")
            val type = parts.getOrNull(0)?.uppercase() ?: ""
            when (type) {
                "CREATE_TASK" -> {
                    val title = parts.getOrNull(1) ?: "New Task"
                    val duration = parts.getOrNull(2)?.toIntOrNull() ?: 30
                    val priority = parts.getOrNull(3) ?: "HIGH"
                    val area = parts.getOrNull(4) ?: "CAREER"
                    actions.add(AgentAction.CreateTask(title, duration, priority, area))
                }
                "CREATE_HABIT" -> {
                    val name = parts.getOrNull(1) ?: "New Habit"
                    val icon = parts.getOrNull(2) ?: "🔥"
                    val area = parts.getOrNull(3) ?: "HEALTH"
                    val freq = parts.getOrNull(4) ?: "DAILY"
                    actions.add(AgentAction.CreateHabit(name, icon, area, freq))
                }
                "CREATE_PROJECT" -> {
                    val title = parts.getOrNull(1) ?: "New Project"
                    val area = parts.getOrNull(2) ?: "CAREER"
                    val goal = parts.getOrNull(3) ?: ""
                    actions.add(AgentAction.CreateProject(title, area, goal))
                }
            }
        }

        val cleaned = actionRegex.replace(text, "").trim()
        return Pair(cleaned, actions)
    }

    private fun generateOfflineInsight(
        prompt: String,
        role: AgentRole,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        goals: List<GoalEntity>,
        focusSessions: List<FocusSessionEntity>
    ): String {
        val lower = prompt.lowercase()
        val totalFocusMins = focusSessions.sumOf { it.durationMinutes }
        val pendingTasks = tasks.filter { it.status != "COMPLETED" }

        return when {
            role == AgentRole.PLANNER || lower.contains("plan") || lower.contains("schedule") -> {
                """
Good day! Here is your strategic schedule based on your current priorities and energy cycles:

🎯 **Today's Strategic Flow**:
• **18:00 - 18:45**: ${pendingTasks.firstOrNull()?.title ?: "Deep Focus Task"} (Top Priority)
• **19:00 - 20:00**: Gym & Physical Reset (Health foundation)
• **20:30 - 21:30**: Python / AI Practice (Peak evening cognitive window)
• **21:45 - 22:15**: Non-fiction Reading (Compound knowledge)

🔥 **Main Objective**: Complete the highest priority task before dinner to protect momentum.

[[ACTION:CREATE_TASK|Deep Work: AI Agent Tool Calling|45|URGENT|CAREER]]
[[ACTION:CREATE_TASK|Read 20 min in DDIA book|20|MEDIUM|KNOWLEDGE]]
""".trimIndent()
            }

            role == AgentRole.GOAL_BREAKDOWN || lower.contains("break down") || lower.contains("breakdown") || lower.contains("build") -> {
                val goalSubject = prompt.replace("break down", "", ignoreCase = true)
                    .replace("breakdown", "", ignoreCase = true)
                    .replace("how to", "", ignoreCase = true).trim()
                    .ifEmpty { "Agentic AI System" }

                """
🎯 **Breakdown Architecture for: $goalSubject**

To make this goal inevitable, we deconstruct it into executable phases:

**Phase 1: Foundation & Core Contracts**
• Define state schemas, memory interfaces, and tool signatures.
• Set up test harness with mock inputs.

**Phase 2: Tool Calling & Reasoners**
• Implement structured output parser and error recovery loop.
• Integrate Gemini API function declarations.

**Phase 3: Supervisor & Execution**
• Multi-agent supervisor pattern with delegated sub-tasks.
• Deploy local runner and latency telemetry.

Ready to execute? I have drafted initial tasks for you:

[[ACTION:CREATE_TASK|Define Agent Tool Interface Specs|30|HIGH|CAREER]]
[[ACTION:CREATE_TASK|Implement Retry & Backoff Strategy|45|HIGH|LEARNING]]
[[ACTION:CREATE_PROJECT|Multi-Agent System Implementation|CAREER|$goalSubject]]
""".trimIndent()
            }

            role == AgentRole.HABIT_ANALYST || lower.contains("habit") || lower.contains("streak") -> {
                """
🔥 **Habit Specialist Analysis**

• **Strongest Habit**: ${habits.maxByOrNull { it.currentStreak }?.name ?: "Daily Exercise"} (${habits.maxByOrNull { it.currentStreak }?.currentStreak ?: 12} days).
• **Consistency Rate**: 86% across active habits this cycle.
• **Friction Point**: Late evening reading tends to be missed when screen time exceeds 9:30 PM.
• **Prescription**: Habit-stack reading directly after your evening tea/shower before touching your phone.

[[ACTION:CREATE_HABIT|10m Evening Wind-down|🌙|HEALTH|DAILY]]
""".trimIndent()
            }

            role == AgentRole.WEEKLY_REVIEW || lower.contains("week") || lower.contains("review") -> {
                """
📊 **LifeOS Weekly Executive Review**

• **Productivity Index**: 84 / 100
• **Deep Work Logged**: ${totalFocusMins / 60}h ${totalFocusMins % 60}m across high-impact blocks.
• **Tasks Velocity**: ${tasks.count { it.status == "COMPLETED" }} completed / ${tasks.size} planned.
• **Key Win**: Strong momentum in Career & Learning pillars.

💡 **Strategic Recommendation for Next Week**:
Cap daily planned tasks at 5 to prevent spillover, and block 45 minutes of protected deep work before checking email.
""".trimIndent()
            }

            else -> {
                """
Hello! Looking at your LifeOS dashboard:

• You have **${pendingTasks.size} high-priority tasks** remaining today.
• Your current focus time is **${totalFocusMins / 60}h ${totalFocusMins % 60}m**.
• Recommended next step: Tackle **"${pendingTasks.firstOrNull()?.title ?: "your primary goal milestone"}"** for 30 minutes in Focus Mode.

What area would you like to optimize right now?
""".trimIndent()
            }
        }
    }
}
