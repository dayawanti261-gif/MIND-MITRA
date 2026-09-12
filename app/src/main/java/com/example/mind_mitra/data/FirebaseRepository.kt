package com.example.mind_mitra.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    // =========================
    // USER PROFILE
    // =========================

    fun saveUserProfile(
        userId: String,
        name: String,
        language: String,
        email: String,
        connectionPin: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "language" to language,
            "connectionPin" to connectionPin,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getUserProfile(
        userId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.data)
            }
            .addOnFailureListener { onError(it) }
    }

    fun profileExists(
        userId: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // CAREGIVER CONNECTION
    // =========================

    fun findPatientByEmailAndPin(
        email: String,
        connectionPin: String,
        onSuccess: (String?, Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("email", email.trim())
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    onSuccess(null, null)
                } else {

                    val document = result.documents[0]
                    val data = document.data

                    val storedPin =
                        data?.get("connectionPin") as? String

                    if (storedPin == connectionPin.trim()) {
                        onSuccess(document.id, data)
                    } else {
                        onSuccess(null, null)
                    }
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun linkCaregiverToPatient(
        caregiverId: String,
        patientId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val caregiver = hashMapOf(
            "patientId" to patientId
        )

        db.collection("caregivers")
            .document(caregiverId)
            .set(caregiver)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getLinkedPatientId(
        caregiverId: String,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("caregivers")
            .document(caregiverId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.getString("patientId"))
            }
            .addOnFailureListener {
                onError(it)
            }
    }
    fun getConnectedPatientProfile(
        caregiverId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(null)
                    return@getLinkedPatientId
                }

                getUserProfile(
                    patientId,
                    onSuccess = { profile ->
                        onSuccess(profile)
                    },
                    onError = { onError(it) }
                )
            },
            onError = { onError(it) }
        )
    }

    // =========================
    // CAREGIVER - PATIENT DATA
    // =========================

    fun getConnectedPatientSchedule(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(emptyList())
                    return@getLinkedPatientId
                }

                db.collection("users")
                    .document(patientId)
                    .collection("schedule")
                    .get()
                    .addOnSuccessListener { result ->
                        onSuccess(
                            result.documents.mapNotNull { it.data }
                        )
                    }
                    .addOnFailureListener { onError(it) }
            },
            onError = { onError(it) }
        )
    }

    fun getConnectedPatientProgress(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(emptyList())
                    return@getLinkedPatientId
                }

                db.collection("users")
                    .document(patientId)
                    .collection("progress")
                    .get()
                    .addOnSuccessListener { result ->
                        onSuccess(
                            result.documents.mapNotNull { it.data }
                        )
                    }
                    .addOnFailureListener { onError(it) }
            },
            onError = { onError(it) }
        )
    }

    fun getConnectedPatientMemories(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(emptyList())
                    return@getLinkedPatientId
                }

                db.collection("users")
                    .document(patientId)
                    .collection("memories")
                    .get()
                    .addOnSuccessListener { result ->
                        onSuccess(
                            result.documents.mapNotNull { it.data }
                        )
                    }
                    .addOnFailureListener { onError(it) }
            },
            onError = { onError(it) }
        )
    }

    fun getConnectedPatientFamily(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(emptyList())
                    return@getLinkedPatientId
                }

                db.collection("users")
                    .document(patientId)
                    .collection("family")
                    .get()
                    .addOnSuccessListener { result ->
                        onSuccess(
                            result.documents.mapNotNull { it.data }
                        )
                    }
                    .addOnFailureListener { onError(it) }
            },
            onError = { onError(it) }
        )
    }

    fun getConnectedPatientReminders(
        caregiverId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getLinkedPatientId(
            caregiverId,
            onSuccess = { patientId ->

                if (patientId == null) {
                    onSuccess(emptyList())
                    return@getLinkedPatientId
                }

                db.collection("users")
                    .document(patientId)
                    .collection("reminders")
                    .get()
                    .addOnSuccessListener { result ->
                        onSuccess(
                            result.documents.mapNotNull { it.data }
                        )
                    }
                    .addOnFailureListener { onError(it) }
            },
            onError = { onError(it) }
        )
    }

    // =========================
    // FAMILY
    // =========================

    fun addFamilyMember(
        userId: String,
        name: String,
        relation: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val familyMember = hashMapOf(
            "name" to name,
            "relation" to relation
        )

        db.collection("users")
            .document(userId)
            .collection("family")
            .add(familyMember)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getFamilyMembers(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("family")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(
                    result.documents.mapNotNull { it.data }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // MEMORIES
    // =========================

    fun addMemory(
        userId: String,
        title: String,
        description: String,
        people: List<String>,
        imageUrl: String = "",
        voiceUrl: String = "",
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val memory = hashMapOf(
            "title" to title,
            "description" to description,
            "people" to people,
            "imageUrl" to imageUrl,
            "voiceUrl" to voiceUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("memories")
            .add(memory)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getMemories(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(
                    result.documents.mapNotNull { it.data }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // DAILY ROUTINE
    // =========================

    fun addSchedule(
        userId: String,
        title: String,
        time: String,
        completed: Boolean = false,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val schedule = hashMapOf(
            "title" to title,
            "time" to time,
            "completed" to completed
        )

        db.collection("users")
            .document(userId)
            .collection("schedule")
            .add(schedule)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getSchedule(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("schedule")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(
                    result.documents.mapNotNull { it.data }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // REMINDERS
    // =========================

    fun addReminder(
        userId: String,
        title: String,
        time: String,
        completed: Boolean = false,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val reminder = hashMapOf(
            "title" to title,
            "time" to time,
            "completed" to completed
        )

        db.collection("users")
            .document(userId)
            .collection("reminders")
            .add(reminder)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getReminders(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("reminders")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(
                    result.documents.mapNotNull { it.data }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // GAME PROGRESS
    // =========================

    fun saveGameProgress(
        userId: String,
        gameId: String,
        accuracy: Double,
        level: Int,
        attempts: Int,
        completionTime: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val progress = hashMapOf(
            "accuracy" to accuracy,
            "level" to level,
            "attempts" to attempts,
            "completionTime" to completionTime,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("progress")
            .document(gameId)
            .set(progress)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getGameProgress(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("progress")
            .get()
            .addOnSuccessListener { result ->
                onSuccess(
                    result.documents.mapNotNull { it.data }
                )
            }
            .addOnFailureListener { onError(it) }
    }

    // =========================
    // PREFERENCES
    // =========================

    fun savePreferences(
        userId: String,
        favouriteMusic: String,
        favouriteActivities: String,
        favouriteMemories: String,
        language: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val preferences = hashMapOf(
            "favouriteMusic" to favouriteMusic,
            "favouriteActivities" to favouriteActivities,
            "favouriteMemories" to favouriteMemories,
            "language" to language
        )

        db.collection("users")
            .document(userId)
            .collection("preferences")
            .document("settings")
            .set(preferences)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getPreferences(
        userId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("preferences")
            .document("settings")
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.data)
            }
            .addOnFailureListener { onError(it) }
    }
}