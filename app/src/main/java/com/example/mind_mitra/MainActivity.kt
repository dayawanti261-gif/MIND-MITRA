package com.example.mind_mitra

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.example.mind_mitra.auth.LanguageSelectionScreen
import com.example.mind_mitra.auth.LoginScreen
import com.example.mind_mitra.auth.RoleSelectionScreen
import com.example.mind_mitra.auth.UserProfileScreen
import com.example.mind_mitra.auth.WelcomeScreen
import com.example.mind_mitra.caregiver.CaregiverDashboardScreen
import com.example.mind_mitra.caregiver.ConnectPatientScreen
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.locale.LocaleHelper
import com.example.mind_mitra.ui.theme.MINDMITRATheme
import com.example.mind_mitra.user.UserHomeScreen

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applySavedLocale(this)

        setContent {
            val language = LocaleHelper.getSavedLanguage(this)
            key(language) {
                MINDMITRATheme {
                    MindMitraApp()
                }
            }
        }
    }
}

private fun resolveRole(profile: Map<String, Any>?): String {
    if (profile == null) return "User"
    val explicit = profile["role"] as? String
    if (!explicit.isNullOrBlank()) return explicit
    return if (profile["linkedPatientId"] != null) "Caregiver" else "User"
}

@Composable
fun MindMitraApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("checking") }
    var selectedRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var caregiverName by remember { mutableStateOf("Caregiver") }

    fun handleLogout() {
        AuthRepository.logout()
        selectedRole = ""
        userName = ""
        caregiverName = "Caregiver"
        currentScreen = "language"
    }

    fun routeFromProfile(profile: Map<String, Any>?, userId: String) {
        if (profile == null) {
            FirebaseRepository.getLinkedPatientId(
                caregiverId = userId,
                onSuccess = { patientId ->
                    if (patientId != null) {
                        selectedRole = "Caregiver"
                        currentScreen = "caregiver_home"
                    } else {
                        currentScreen = "role_selection"
                    }
                },
                onError = {
                    currentScreen = "role_selection"
                }
            )
            return
        }

        val role = resolveRole(profile)
        selectedRole = role
        val name = profile["name"] as? String ?: ""

        when (role) {
            "Caregiver" -> {
                caregiverName = name.ifBlank { "Caregiver" }
                val linkedPatientId = profile["linkedPatientId"] as? String
                currentScreen =
                    if (linkedPatientId != null) "caregiver_home"
                    else "connect_patient"
            }
            else -> {
                userName = name
                currentScreen = "user_home"
            }
        }
    }

    LaunchedEffect(Unit) {
        val userId = AuthRepository.getCurrentUserId()
        if (userId == null) {
            currentScreen = if (LocaleHelper.hasSelectedLanguage(context)) "welcome" else "language"
        } else {
            FirebaseRepository.getUserProfile(
                userId = userId,
                onSuccess = { profile -> routeFromProfile(profile, userId) },
                onError = { currentScreen = "welcome" }
            )
        }
    }

    when (currentScreen) {
        "checking" -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        "language" -> {
            LanguageSelectionScreen(
                onContinue = { /* activity recreates */ }
            )
        }
        "welcome" -> {
            WelcomeScreen(onGetStarted = { currentScreen = "role_selection" })
        }
        "role_selection" -> {
            RoleSelectionScreen(
                onUserSelected = { selectedRole = "User"; currentScreen = "login" },
                onCaregiverSelected = { selectedRole = "Caregiver"; currentScreen = "login" }
            )
        }
        "login" -> {
            LoginScreen(
                role = selectedRole,
                onBack = { currentScreen = "role_selection" },
                onLoginSuccess = {
                    val userId = AuthRepository.getCurrentUserId()
                    if (userId == null) {
                        currentScreen = if (selectedRole == "User") "user_profile" else "connect_patient"
                        return@LoginScreen
                    }
                    if (selectedRole == "User") {
                        FirebaseRepository.getUserProfile(
                            userId = userId,
                            onSuccess = { profile ->
                                if (profile != null) {
                                    userName = profile["name"] as? String ?: ""
                                    currentScreen = "user_home"
                                } else currentScreen = "user_profile"
                            },
                            onError = { currentScreen = "user_profile" }
                        )
                    } else {
                        FirebaseRepository.getUserProfile(
                            userId = userId,
                            onSuccess = { profile -> routeFromProfile(profile, userId) },
                            onError = { currentScreen = "connect_patient" }
                        )
                    }
                },
                onSignUpSuccess = {
                    val userId = AuthRepository.getCurrentUserId()
                    if (selectedRole == "User") {
                        currentScreen = "user_profile"
                    } else if (userId != null) {
                        FirebaseRepository.saveCaregiverProfile(
                            userId = userId,
                            email = AuthRepository.getCurrentUserEmail() ?: "",
                            onSuccess = { currentScreen = "connect_patient" },
                            onError = { currentScreen = "connect_patient" }
                        )
                    } else currentScreen = "connect_patient"
                }
            )
        }
        "user_profile" -> {
            UserProfileScreen(onProfileCompleted = { name ->
                userName = name
                currentScreen = "user_home"
            })
        }
        "user_home" -> {
            UserHomeScreen(
                userName = userName,
                onLogout = { handleLogout() },
                onChangeLanguage = { currentScreen = "language" }
            )
        }
        "connect_patient" -> {
            ConnectPatientScreen(onConnected = { currentScreen = "caregiver_home" })
        }
        "caregiver_home" -> {
            CaregiverDashboardScreen(
                caregiverName = caregiverName,
                onLogout = { handleLogout() },
                onChangeLanguage = { currentScreen = "language" }
            )
        }
    }
}
