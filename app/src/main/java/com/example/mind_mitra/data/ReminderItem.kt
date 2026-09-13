package com.example.mind_mitra.data

data class ReminderItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val repeat: String = "none",
    val enabled: Boolean = true,
    val lastCompletedDate: String? = null
) {
    val completedToday: Boolean
        get() = lastCompletedDate == todayDateString()
}
