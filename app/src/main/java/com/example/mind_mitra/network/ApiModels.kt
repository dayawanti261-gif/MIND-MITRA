package com.example.mind_mitra.network

data class RoutineResponse(
    val routine: List<RoutineData>
)

data class RoutineData(
    val id: String?,
    val title: String?,
    val time: String?,
    val daysOfWeek: String? = null,
    val days_of_week: String? = null,
    val enabled: Boolean? = null,
    val reminderNote: String? = null,
    val reminder_note: String? = null,
    val lastCompletedDate: String?,
    val timestamp: Long?
) {
    val completedToday: Boolean
        get() {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            return lastCompletedDate == today
        }
}

data class GameProgressRequest(
    val user_id: String,
    val game_id: String,
    val accuracy: Float,
    val time_taken: Float,
    val mistakes: Int,
    val attempts: Int,
    val level: Int,
    val completed: Boolean
)

data class GameProgressResponse(
    val game_progress: List<GameProgressData>
)

data class GameProgressData(
    val id: String?,
    val game_id: String?,
    val accuracy: Float?,
    val time_taken: Float?,
    val mistakes: Int?,
    val attempts: Int?,
    val level: Int?,
    val completed: Boolean?
)

data class LinkPatientRequest(
    val patient_email: String,
    val connection_pin: String
)

data class LinkPatientResponse(
    val message: String?,
    val patient_id: String?,
    val patient_name: String?
)

data class ReminderRequest(
    val user_id: String,
    val title: String,
    val time: String,
    val description: String? = null,
    val date: String? = null,
    val repeat: String = "none",
    val enabled: Boolean = true
)

data class ReminderResponse(
    val reminders: List<ReminderData>
)

data class ReminderData(
    val id: String?,
    val title: String?,
    val description: String?,
    val date: String?,
    val time: String?,
    val repeat: String?,
    val enabled: Boolean?,
    val lastCompletedDate: String?,
    val createdBy: String?,
    val timestamp: Long?
)

data class GameResultEntry(
    val score: Float = 0f,
    val accuracy: Float = 0f,
    val mistakes: Int = 0,
    val completed: Boolean = false
)

data class DifficultyRequest(
    val game_type: String,
    val recent_results: List<GameResultEntry>
)

data class DifficultyResponse(
    val difficulty: String,
    val source: String? = null
)

data class AgentChatRequest(
    val message: String,
    val language: String? = null
)

data class AgentChatResponse(
    val message: String?,
    val action: AgentAction?
)

data class AgentAction(
    val type: String?,
    val payload: Map<String, Any?>?
)
