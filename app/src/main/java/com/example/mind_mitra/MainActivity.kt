package com.example.mind_mitra

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.auth.LoginScreen
import com.example.mind_mitra.auth.RoleSelectionScreen
import com.example.mind_mitra.auth.UserProfileScreen
import com.example.mind_mitra.auth.WelcomeScreen
import com.example.mind_mitra.caregiver.CaregiverDashboardScreen
import com.example.mind_mitra.caregiver.ConnectPatientScreen
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.User.UserHomeScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MindMitraApp()
        }
    }
}


@Composable
fun MindMitraApp() {

    var currentScreen by remember {
        mutableStateOf("checking")
    }

    var selectedRole by remember {
        mutableStateOf("")
    }

    var userName by remember {
        mutableStateOf("")
    }


    // Check Firebase session when app starts
    LaunchedEffect(Unit) {

        val userId = AuthRepository.getCurrentUserId()

        if (userId == null) {

            // No logged-in user
            currentScreen = "welcome"

        } else {

            // User is already logged in
            FirebaseRepository.getUserProfile(
                userId = userId,

                onSuccess = { profile ->

                    if (profile != null) {

                        // Profile already exists
                        selectedRole = "User"

                        userName = profile["name"] as? String ?: ""

                        currentScreen = "user_home"

                    } else {

                        // Logged in but profile not created yet
                        selectedRole = "User"

                        currentScreen = "user_profile"
                    }
                },

                onError = {

                    // If Firebase check fails, show welcome
                    currentScreen = "welcome"
                }
            )
        }
    }


    when (currentScreen) {

        // --------------------------------
        // CHECKING SESSION
        // --------------------------------

        "checking" -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }


        // --------------------------------
        // WELCOME
        // --------------------------------

        "welcome" -> {

            WelcomeScreen(
                onGetStarted = {
                    currentScreen = "role_selection"
                }
            )
        }


        // --------------------------------
        // ROLE SELECTION
        // --------------------------------

        "role_selection" -> {

            RoleSelectionScreen(

                onUserSelected = {

                    selectedRole = "User"
                    currentScreen = "login"
                },

                onCaregiverSelected = {

                    selectedRole = "Caregiver"
                    currentScreen = "login"
                }
            )
        }


        // --------------------------------
        // LOGIN / SIGN UP
        // --------------------------------

        "login" -> {

            LoginScreen(

                role = selectedRole,

                onBack = {
                    currentScreen = "role_selection"
                },

                onLoginSuccess = {

                    if (selectedRole == "User") {

                        val userId = AuthRepository.getCurrentUserId()

                        if (userId != null) {

                            FirebaseRepository.getUserProfile(
                                userId = userId,

                                onSuccess = { profile ->

                                    if (profile != null) {

                                        // Existing user
                                        userName =
                                            profile["name"] as? String ?: ""

                                        currentScreen = "user_home"

                                    } else {

                                        // New user without profile
                                        currentScreen = "user_profile"
                                    }
                                },

                                onError = {

                                    currentScreen = "user_profile"
                                }
                            )

                        } else {

                            currentScreen = "user_profile"
                        }

                    } else {

                        val caregiverId = AuthRepository.getCurrentUserId()

                        if (caregiverId == null) {

                            currentScreen = "connect_patient"

                        } else {

                            FirebaseRepository.getLinkedPatientId(
                                caregiverId = caregiverId,

                                onSuccess = { patientId ->
                                    println("CONNECTED PATIENT ID = $patientId")

                                    currentScreen =
                                        if (patientId != null)
                                            "caregiver_home"
                                        else
                                            "connect_patient"
                                },

                                onError = {

                                    currentScreen = "connect_patient"
                                }
                            )
                        }
                    }
                },


                onSignUpSuccess = {

                    if (selectedRole == "User") {

                        // New user must complete profile
                        currentScreen = "user_profile"

                    } else {

                        // New caregiver has no linked patient yet
                        currentScreen = "connect_patient"
                    }
                }
            )
        }


        // --------------------------------
        // USER PROFILE
        // --------------------------------

        "user_profile" -> {

            UserProfileScreen(

                onProfileCompleted = { name ->

                    userName = name

                    currentScreen = "user_home"
                }
            )
        }


        // --------------------------------
        // USER HOME
        // --------------------------------

        "user_home" -> {

            UserHomeScreen(
                userName = userName
            )
        }


        // --------------------------------
        // CONNECT PATIENT
        // --------------------------------

        "connect_patient" -> {

            ConnectPatientScreen(
                onConnected = {
                    currentScreen = "caregiver_home"
                }
            )
        }


        // --------------------------------
        // CAREGIVER HOME
        // --------------------------------

        "caregiver_home" -> {

            CaregiverDashboardScreen(
                caregiverName = "Caregiver"
            )
        }
    }
}