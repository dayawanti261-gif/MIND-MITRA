package com.example.mind_mitra.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.data.MemoryItem
import com.example.mind_mitra.data.ProgressStats
import com.example.mind_mitra.data.ReminderCache
import com.example.mind_mitra.data.ReminderItem
import com.example.mind_mitra.data.RoutineCache
import com.example.mind_mitra.data.RoutineItem
import com.example.mind_mitra.network.RetrofitClient
import com.example.mind_mitra.notifications.ReminderScheduler
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserHomeUiState(
    val userName: String = "",
    val routines: List<RoutineItem> = emptyList(),
    val reminders: List<ReminderItem> = emptyList(),
    val memories: List<MemoryItem> = emptyList(),
    val progressStats: ProgressStats = ProgressStats(),
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class UserHomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeUiState())
    val uiState: StateFlow<UserHomeUiState> = _uiState.asStateFlow()

    private var routineListener: ListenerRegistration? = null
    private var reminderListener: ListenerRegistration? = null
    private var memoryListener: ListenerRegistration? = null
    private var activeUserId: String? = null

    fun loadUserData() {
        val userId = AuthRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        if (userId == activeUserId && routineListener != null) {
            return
        }
        activeUserId = userId
        _uiState.value = _uiState.value.copy(isLoading = true)

        FirebaseRepository.getUserProfile(
            userId = userId,
            onSuccess = { profile ->
                val name = profile?.get("name") as? String ?: ""
                _uiState.value = _uiState.value.copy(userName = name)
            },
            onError = { }
        )

        routineListener?.remove()
        routineListener = FirebaseRepository.listenToRoutines(
            userId = userId,
            onUpdate = { routineList ->
                _uiState.value = _uiState.value.copy(
                    routines = routineList,
                    isLoading = false,
                    isOffline = false,
                    errorMessage = null
                )
            },
            onError = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = true
                )
                loadRoutinesFromApi(userId, replaceOnlyWhenNonEmpty = false)
            }
        )

        reminderListener?.remove()
        reminderListener = FirebaseRepository.listenToReminders(
            userId = userId,
            onUpdate = { reminderList ->
                _uiState.value = _uiState.value.copy(
                    reminders = reminderList,
                    isOffline = false
                )
            },
            onError = {
                loadRemindersFromApi(userId, replaceOnlyWhenNonEmpty = false)
            }
        )

        memoryListener?.remove()
        memoryListener = FirebaseRepository.listenToMemories(
            userId = userId,
            onUpdate = { memoryList ->
                _uiState.value = _uiState.value.copy(memories = memoryList)
            },
            onError = { }
        )

        FirebaseRepository.getProgressStats(
            userId = userId,
            onSuccess = { stats ->
                _uiState.value = _uiState.value.copy(progressStats = stats)
            },
            onError = { }
        )

        loadGameProgressFromApi(userId)
    }

    fun ensureDataLoaded() {
        loadUserData()
    }

    fun scheduleAlarms(context: Context) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        val reminders = _uiState.value.reminders
        try {
            ReminderCache.save(context, userId, reminders)
            ReminderScheduler.scheduleReminders(context, reminders)
        } catch (_: Exception) {
            // Alarm scheduling is best-effort; never crash the dashboard.
        }
    }

    fun refreshRoutines() {
        val userId = AuthRepository.getCurrentUserId() ?: return
        if (_uiState.value.routines.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }
        loadRoutinesFromApi(userId, replaceOnlyWhenNonEmpty = true)
    }

    fun loadCachedRoutines(context: Context) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        if (_uiState.value.routines.isEmpty()) {
            val cached = RoutineCache.load(context, userId)
            if (cached.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    routines = cached,
                    isOffline = true,
                    isLoading = false
                )
            }
        }
    }

    fun cacheRoutines(context: Context) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        val routines = _uiState.value.routines
        if (routines.isNotEmpty()) {
            RoutineCache.save(context, userId, routines)
        }
    }

    private fun loadRoutinesFromApi(userId: String, replaceOnlyWhenNonEmpty: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUserRoutine(userId)
                val routines = response.routine.mapNotNull { data ->
                    val id = data.id ?: return@mapNotNull null
                    RoutineItem(
                        id = id,
                        title = data.title ?: "",
                        time = data.time ?: "",
                        daysOfWeek = data.daysOfWeek ?: data.days_of_week ?: "Every day",
                        enabled = data.enabled ?: true,
                        reminderNote = data.reminderNote ?: data.reminder_note ?: "",
                        lastCompletedDate = data.lastCompletedDate,
                        timestamp = data.timestamp ?: 0L
                    )
                }
                withContext(Dispatchers.Main) {
                    if (replaceOnlyWhenNonEmpty && routines.isEmpty() && _uiState.value.routines.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@withContext
                    }
                    if (routines.isNotEmpty() || _uiState.value.routines.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            routines = routines,
                            isLoading = false,
                            isOffline = false,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOffline = true
                    )
                }
            }
        }
    }

    private fun loadRemindersFromApi(userId: String, replaceOnlyWhenNonEmpty: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUserReminders(userId)
                val reminders = response.reminders.mapNotNull { data ->
                    ReminderItem(
                        id = data.id ?: return@mapNotNull null,
                        title = data.title ?: "",
                        description = data.description ?: "",
                        date = data.date ?: "",
                        time = data.time ?: "",
                        repeat = data.repeat ?: "none",
                        enabled = data.enabled ?: true,
                        lastCompletedDate = data.lastCompletedDate
                    )
                }
                withContext(Dispatchers.Main) {
                    if (replaceOnlyWhenNonEmpty && reminders.isEmpty() && _uiState.value.reminders.isNotEmpty()) {
                        return@withContext
                    }
                    if (reminders.isNotEmpty() || _uiState.value.reminders.isEmpty()) {
                        _uiState.value = _uiState.value.copy(reminders = reminders, isOffline = false)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isOffline = true)
                }
            }
        }
    }

    private fun loadGameProgressFromApi(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getGameProgress(userId)
                val progress = response.game_progress
                val memory = progress.find { it.game_id == "memory_match" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.memoryGame
                val pattern = progress.find { it.game_id == "sequence" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.patternGame
                val recall = progress.find { it.game_id == "recall" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.recallGame
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        progressStats = _uiState.value.progressStats.copy(
                            memoryGame = memory,
                            patternGame = pattern,
                            recallGame = recall,
                            activitiesCompleted = progress.size
                        ),
                        isOffline = false
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isOffline = true)
                }
            }
        }
    }

    fun getReminders(): List<Map<String, Any>> {
        return _uiState.value.reminders.map { reminder ->
            mapOf(
                "id" to reminder.id,
                "title" to reminder.title,
                "time" to reminder.time,
                "description" to reminder.description,
                "isCompleted" to reminder.completedToday,
                "status" to if (reminder.completedToday) "Done" else "Pending"
            )
        }
    }

    fun getMemories(): List<Map<String, Any>> {
        return _uiState.value.memories.map { memory ->
            mapOf(
                "id" to memory.id,
                "title" to memory.title,
                "category" to memory.category,
                "description" to memory.description,
                "photo_path" to memory.photo_path
            )
        }
    }

    fun getSchedule(): List<Map<String, Any>> {
        return _uiState.value.routines.map { routine ->
            mapOf(
                "id" to routine.id,
                "title" to routine.title,
                "time" to routine.time,
                "isCompleted" to routine.completed,
                "status" to if (routine.completed) "Done" else "Pending"
            )
        }
    }

    fun getProgressStats(): ProgressStats = _uiState.value.progressStats

    fun getUserName(): String = _uiState.value.userName.ifEmpty { "User" }

    fun toggleRoutine(routineId: String, isCurrentlyCompleted: Boolean) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        FirebaseRepository.toggleRoutineCompletion(
            userId = userId,
            routineId = routineId,
            isCompleted = !isCurrentlyCompleted,
            onSuccess = { },
            onError = { }
        )
    }

    fun markReminderComplete(reminderId: String) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        FirebaseRepository.markReminderComplete(
            userId = userId,
            reminderId = reminderId,
            onSuccess = { },
            onError = { }
        )
    }

    fun addRoutine(title: String, time: String) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        FirebaseRepository.addRoutineItem(
            userId = userId,
            title = title,
            time = time,
            onSuccess = { },
            onError = { }
        )
    }

    fun addMemory(title: String, category: String, description: String) {
        val userId = AuthRepository.getCurrentUserId() ?: return
        FirebaseRepository.addMemory(
            userId = userId,
            title = title,
            category = category,
            description = description,
            onSuccess = { },
            onError = { }
        )
    }

    override fun onCleared() {
        super.onCleared()
        routineListener?.remove()
        reminderListener?.remove()
        memoryListener?.remove()
        activeUserId = null
    }
}
