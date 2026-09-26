package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LifeArea(
    val title: String,
    val emoji: String,
    val color: Color
) {
    CAREER("Career", "🏢", Color(0xFF3B82F6)),
    LEARNING("Learning", "🧠", Color(0xFF8B5CF6)),
    HEALTH("Health", "💪", Color(0xFF10B981)),
    FINANCE("Finance", "💰", Color(0xFFF59E0B)),
    FAMILY("Family", "👨‍👩‍👧", Color(0xFFEC4899)),
    RELATIONSHIPS("Relationships", "❤️", Color(0xFFEF4444)),
    PERSONAL("Personal", "🎨", Color(0xFF06B6D4)),
    KNOWLEDGE("Knowledge", "📚", Color(0xFF6366F1));

    companion object {
        fun fromString(value: String): LifeArea {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: CAREER
        }
    }
}

enum class Priority(val title: String, val level: Int, val color: Color) {
    LOW("Low", 1, Color(0xFF10B981)),
    MEDIUM("Medium", 2, Color(0xFFF59E0B)),
    HIGH("High", 3, Color(0xFFF97316)),
    URGENT("Urgent", 4, Color(0xFFEF4444));

    companion object {
        fun fromString(value: String): Priority {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: MEDIUM
        }
    }
}

enum class TaskStatus(val title: String) {
    INBOX("Inbox"),
    PLANNED("Planned"),
    TODAY("Today"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    DEFERRED("Deferred");

    companion object {
        fun fromString(value: String): TaskStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.title.equals(value, ignoreCase = true) }
                ?: TODAY
        }
    }
}

enum class HabitFrequency(val title: String) {
    DAILY("Daily"),
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    WEEKLY("Weekly");

    companion object {
        fun fromString(value: String): HabitFrequency {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}

enum class Mood(val title: String, val emoji: String) {
    GREAT("Great", "🤩"),
    GOOD("Good", "😊"),
    NEUTRAL("Neutral", "😐"),
    TIRED("Tired", "🥱"),
    STRESSED("Stressed", "😓");

    companion object {
        fun fromString(value: String): Mood {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GOOD
        }
    }
}
