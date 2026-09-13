package com.example.mind_mitra.network

data class RoutineResponse(
    val routine: List<RoutineData>
)

data class RoutineData(
    val id: String?,
    val title: String?,
    val time: String?,
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

data class AgentChatRequest(
    val user_id: String,
    val message: String
)

data class AgentChatResponse(
    val message: String?,
    val action: AgentAction?
)

data class AgentAction(
    val type: String?,
    val payload: Map<String, Any?>?
)
