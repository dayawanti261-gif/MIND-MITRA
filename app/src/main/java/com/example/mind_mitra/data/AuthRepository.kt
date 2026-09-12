package com.example.mind_mitra.data

import com.google.firebase.auth.FirebaseAuth

object AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    fun signUp(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onError(Exception("User ID not found"))
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onError(Exception("User ID not found"))
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}