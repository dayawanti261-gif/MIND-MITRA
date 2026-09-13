package com.example.mind_mitra.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

// ============================================================================
// DATA MODELS WITH OPERATOR BRACKET ACCESS SUPPORT
// ============================================================================

data class RoutineItem(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val daysOfWeek: String = "Every day",
    val enabled: Boolean = true,
    val reminderNote: String = "",
    // Date (yyyy-MM-dd) the activity was last marked done, or null if never.
    // "completed" is derived from this instead of being stored directly, so
    // the activity automatically counts as not-done again once the date
    // rolls over — no daily reset job needed.
    val lastCompletedDate: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val completed: Boolean
        get() = lastCompletedDate == todayDateString()

    operator fun get(key: String): Any? = when (key) {
        "id" -> id
        "title" -> title
        "time" -> time
        "isCompleted", "completed" -> completed
        "status" -> if (completed) "Done" else "Pending"
        else -> null
    }
}

fun todayDateString(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date())

data class MemoryItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val photo_path: String = "",
    val people: List<String> = emptyList()
) {
    operator fun get(key: String): Any? = when (key) {
        "id" -> id
        "title" -> title
        "category" -> category
        "description" -> description
        "photo_path" -> photo_path
        "people" -> people
        else -> null
    }
}

data class ProgressStats(
    val memoryGame: Int = 0,
    val patternGame: Int = 0,
    val recallGame: Int = 0,
    val activitiesCompleted: Int = 0,
    val routineCompletion: Int = 0
) {
    operator fun get(key: String): Any? = when (key) {
        "memoryGame" -> memoryGame
        "patternGame" -> patternGame
        "recallGame" -> recallGame
        "activitiesCompleted" -> activitiesCompleted
        "routineCompletion" -> routineCompletion
        else -> null
    }
}

object FirebaseRepository {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    // --- User Profile ---

    fun saveUserProfile(
        userId: String,
        name: String,
        language: String,
        email: String,
        connectionPin: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val profile = mapOf(
            "uid" to userId,
            "name" to name,
            "language" to language,
            "email" to email.trim().lowercase(),
            "connectionPin" to connectionPin,
            "role" to "User"
        )

        db.collection("users").document(userId)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun saveCaregiverProfile(
        userId: String,
        email: String,
        name: String = "Caregiver",
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val profile = mapOf(
            "uid" to userId,
            "name" to name,
            "email" to email.trim().lowercase(),
            "role" to "Caregiver"
        )
        db.collection("users").document(userId)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getUserProfile(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onSuccess: (Map<String, Any>?) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot -> onSuccess(snapshot.data) }
            .addOnFailureListener { onError(it) }
    }

    // --- Caregiver <-> Patient Linking ---

    fun findPatientByEmailAndPin(
        email: String,
        connectionPin: String,
        onSuccess: (String?, Map<String, Any>?) -> Unit = { _, _ -> },
        onError: (Exception) -> Unit = {}
    ) {
        db.collection("users")
            .whereEqualTo("email", email.trim())
            .whereEqualTo("connectionPin", connectionPin.trim())
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onSuccess(null, null)
                } else {
                    val doc = snapshot.documents.first()
                    onSuccess(doc.id, doc.data)
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun linkCaregiverToPatient(
        caregiverId: String,
        patientId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val batch = db.batch()

        val caregiverRef = db.collection("users").document(caregiverId)
        batch.set(
            caregiverRef,
            mapOf(
                "linkedPatientId" to patientId,
                "role" to "Caregiver",
                "email" to (AuthRepository.getCurrentUserEmail() ?: "")
            ),
            SetOptions.merge()
        )

        val patientRef = db.collection("users").document(patientId)
        batch.set(patientRef, mapOf("linkedCaregiverId" to caregiverId), SetOptions.merge())

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getLinkedPatientId(
        caregiverId: String,
        onSuccess: (String?) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        db.collection("users").document(caregiverId)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.getString("linkedPatientId"))
            }
            .addOnFailureListener { onError(it) }
    }

    private fun withLinkedPatient(
        caregiverId: String,
        onNoPatient: () -> Unit,
        onError: (Exception) -> Unit,
        onPatientId: (String) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId = caregiverId,
            onSuccess = { patientId ->
                if (patientId == null) onNoPatient() else onPatientId(patientId)
            },
            onError = onError
        )
    }

    // --- Caregiver read-through accessors ---

    fun getConnectedPatientProfile(
        caregiverId: String,
        onSuccess: (Map<String, Any>?) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(null) },
            onError = onError
        ) { patientId ->
            getUserProfile(userId = patientId, onSuccess = onSuccess, onError = onError)
        }
    }

    fun getConnectedPatientFamily(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(emptyList()) },
            onError = onError
        ) { patientId ->
            db.collection("users").document(patientId)
                .collection("family")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["id"] = doc.id
                        data
                    }
                    onSuccess(list)
                }
                .addOnFailureListener { onError(it) }
        }
    }

    fun addFamilyMember(
        userId: String,
        name: String,
        relation: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val docRef = db.collection("users").document(userId).collection("family").document()
        val member = mapOf(
            "id" to docRef.id,
            "name" to name,
            "relation" to relation
        )
        docRef.set(member)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getConnectedPatientMemories(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(emptyList()) },
            onError = onError
        ) { patientId ->
            getMemories(userId = patientId, onSuccess = onSuccess, onError = onError)
        }
    }

    fun getConnectedPatientSchedule(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        getConnectedPatientRoutines(caregiverId, onSuccess, onError)
    }

    fun getConnectedPatientRoutines(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(emptyList()) },
            onError = onError
        ) { patientId ->
            db.collection("users").document(patientId)
                .collection("routines")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["id"] = doc.id
                        data
                    }
                    onSuccess(list)
                }
                .addOnFailureListener { onError(it) }
        }
    }

    fun getConnectedPatientReminders(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(emptyList()) },
            onError = onError
        ) { patientId ->
            getReminders(userId = patientId, onSuccess = onSuccess, onError = onError)
        }
    }

    fun addSchedule(
        userId: String,
        title: String,
        time: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        addRoutineItem(userId = userId, title = title, time = time, onSuccess = onSuccess, onError = onError)
    }

    fun addReminder(
        userId: String,
        title: String,
        time: String,
        description: String = "",
        date: String = "",
        repeat: String = "none",
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        val docRef = db.collection("users").document(userId).collection("reminders").document()
        val item = mapOf(
            "id" to docRef.id,
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "repeat" to repeat,
            "enabled" to true,
            "createdBy" to (AuthRepository.getCurrentUserId() ?: ""),
            "lastCompletedDate" to null,
            "timestamp" to System.currentTimeMillis()
        )
        docRef.set(item)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getConnectedPatientProgress(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withLinkedPatient(
            caregiverId = caregiverId,
            onNoPatient = { onSuccess(emptyList()) },
            onError = onError
        ) { patientId ->
            db.collection("users").document(patientId)
                .collection("gameProgress")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["id"] = doc.id
                        data
                    }
                    onSuccess(list)
                }
                .addOnFailureListener { onError(it) }
        }
    }

    // --- Preferences ---

    fun getPreferences(
        userId: String,
        onSuccess: (Map<String, Any>?) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        db.collection("users").document(userId)
            .collection("preferences").document("settings")
            .get()
            .addOnSuccessListener { doc -> onSuccess(doc.data) }
            .addOnFailureListener { onError(it) }
    }

    fun listenToPreferences(
        userId: String,
        onUpdate: (Map<String, Any>?) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        if (userId.isEmpty()) return null
        return db.collection("users").document(userId)
            .collection("preferences").document("settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onUpdate(snapshot?.data)
            }
    }

    fun savePreferences(
        userId: String,
        favouriteMusic: String,
        favouriteActivities: String,
        favouriteMemories: String,
        language: String,
        musicPreferences: List<MusicPreferenceItem> = emptyList(),
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val preferences = mapOf(
            "favouriteMusic" to favouriteMusic,
            "favouriteActivities" to favouriteActivities,
            "favouriteMemories" to favouriteMemories,
            "language" to language,
            "musicPreferences" to musicPreferences.map { it.toMap() }
        )
        db.collection("users").document(userId)
            .collection("preferences").document("settings")
            .set(preferences, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // --- Daily Routines & Reminders ---

    fun getReminders(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        db.collection("users").document(userId)
            .collection("reminders")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data["completed"] = (data["lastCompletedDate"] as? String) == todayDateString()
                    data
                }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it) }
    }

    fun listenToReminders(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onUpdate: (List<ReminderItem>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        if (userId.isEmpty()) return null
        return db.collection("users").document(userId)
            .collection("reminders")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val reminders = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ReminderItem(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        time = data["time"] as? String ?: "",
                        repeat = data["repeat"] as? String ?: "none",
                        enabled = data["enabled"] as? Boolean ?: true,
                        lastCompletedDate = data["lastCompletedDate"] as? String
                    )
                } ?: emptyList()
                onUpdate(reminders)
            }
    }

    fun getSchedule(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        getReminders(userId, onSuccess, onError)
    }

    fun listenToRoutines(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onUpdate: (List<RoutineItem>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        if (userId.isEmpty()) return null
        return db.collection("users").document(userId)
            .collection("routines")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val routines = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        RoutineItem(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            time = data["time"] as? String ?: "",
                            daysOfWeek = data["daysOfWeek"] as? String
                                ?: data["days_of_week"] as? String
                                ?: "Every day",
                            enabled = data["enabled"] as? Boolean ?: true,
                            reminderNote = data["reminderNote"] as? String
                                ?: data["reminder_note"] as? String
                                ?: "",
                            lastCompletedDate = data["lastCompletedDate"] as? String,
                            timestamp = (data["timestamp"] as? Number)?.toLong()
                                ?: System.currentTimeMillis()
                        )
                    } else {
                        doc.toObject(RoutineItem::class.java)?.copy(id = doc.id)
                    }
                } ?: emptyList()

                onUpdate(routines)
            }
    }

    fun markReminderComplete(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        reminderId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        db.collection("users").document(userId)
            .collection("reminders").document(reminderId)
            .update("lastCompletedDate", todayDateString())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun toggleRoutineCompletion(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        routineId: String,
        isCompleted: Boolean,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        db.collection("users").document(userId)
            .collection("routines").document(routineId)
            .update("lastCompletedDate", if (isCompleted) todayDateString() else null)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun addRoutineItem(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        title: String,
        time: String,
        daysOfWeek: String = "Every day",
        enabled: Boolean = true,
        reminderNote: String = "",
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        val docRef = db.collection("users").document(userId).collection("routines").document()
        val item = RoutineItem(
            id = docRef.id,
            title = title,
            time = time,
            daysOfWeek = daysOfWeek,
            enabled = enabled,
            reminderNote = reminderNote,
            lastCompletedDate = null,
            timestamp = System.currentTimeMillis()
        )
        docRef.set(item)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // --- Memory Vault ---

    fun getMemories(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onSuccess: (List<Map<String, Any>>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        db.collection("users").document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                }
                onSuccess(list)
            }
            .addOnFailureListener { onError(it) }
    }

    fun listenToMemories(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onUpdate: (List<MemoryItem>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        if (userId.isEmpty()) return null
        return db.collection("users").document(userId)
            .collection("memories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val memories = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        MemoryItem(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            category = data["category"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            photo_path = data["photo_path"] as? String ?: "",
                            people = (data["people"] as? List<*>)?.mapNotNull { it as? String }
                                ?: emptyList()
                        )
                    } else {
                        doc.toObject(MemoryItem::class.java)?.copy(id = doc.id)
                    }
                } ?: emptyList()

                onUpdate(memories)
            }
    }

    fun addMemory(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        title: String,
        category: String = "",
        description: String,
        photo_path: String = "",
        people: List<String> = emptyList(),
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        val docRef = db.collection("users").document(userId).collection("memories").document()
        val memory = MemoryItem(
            id = docRef.id,
            title = title,
            category = category,
            description = description,
            photo_path = photo_path,
            people = people
        )
        docRef.set(memory)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    // --- Progress & Rewards ---

    fun getProgressStats(
        userId: String = AuthRepository.getCurrentUserId() ?: "",
        onSuccess: (ProgressStats) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (userId.isEmpty()) return
        db.collection("users").document(userId)
            .collection("progress").document("stats")
            .get()
            .addOnSuccessListener { doc ->
                val stats = doc.toObject(ProgressStats::class.java) ?: ProgressStats()
                onSuccess(stats)
            }
            .addOnFailureListener { onError(it) }
    }
}