package com.example.mind_mitra.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.mind_mitra.network.RetrofitClient
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.data.MemoryItem
import com.example.mind_mitra.data.ProgressStats
import com.example.mind_mitra.data.RoutineItem
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserHomeUiState(
    val userName: String = "",
    val routines: List<RoutineItem> = emptyList(),
    val memories: List<MemoryItem> = emptyList(),
    val progressStats: ProgressStats = ProgressStats(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class UserHomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeUiState())
    val uiState: StateFlow<UserHomeUiState> = _uiState.asStateFlow()

    private var routineListener: ListenerRegistration? = null
    private var memoryListener: ListenerRegistration? = null

    init {
        loadUserData()
    }

    fun loadUserData() {
        val userId = AuthRepository.getCurrentUserId() ?: return
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
                    isLoading = false
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.localizedMessage,
                    isLoading = false
                )
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

    private fun loadGameProgressFromApi(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getGameProgress(userId)
                val progress = response.game_progress
                val memory = progress.find { it.game_id == "family_memory_matching" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.memoryGame
                val pattern = progress.find { it.game_id == "pattern_recognition" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.patternGame
                val recall = progress.find { it.game_id == "personal_memory_recall" }
                    ?.accuracy?.toInt() ?: _uiState.value.progressStats.recallGame
                _uiState.value = _uiState.value.copy(
                    progressStats = _uiState.value.progressStats.copy(
                        memoryGame = memory,
                        patternGame = pattern,
                        recallGame = recall,
                        activitiesCompleted = progress.size
                    )
                )
            } catch (_: Exception) {
                // Keep Firestore defaults if API unavailable
            }
        }
    }

    fun getReminders(): List<Map<String, Any>> {
        val routines = _uiState.value.routines
        return routines.map { r ->
            mapOf(
                "id" to r.id,
                "title" to r.title,
                "time" to r.time,
                "isCompleted" to r.completed,
                "status" to if (r.completed) "Done" else "Pending"
            )
        }
    }

    fun getMemories(): List<Map<String, Any>> {
        val memories = _uiState.value.memories
        return memories.map { m ->
            mapOf(
                "id" to m.id,
                "title" to m.title,
                "category" to m.category,
                "description" to m.description,
                "photo_path" to m.photo_path
            )
        }
    }

    fun getSchedule(): List<Map<String, Any>> {
        return getReminders()
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
        memoryListener?.remove()
    }
}