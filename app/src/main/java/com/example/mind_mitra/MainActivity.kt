package com.example.mind_mitra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


import com.example.mind_mitra.auth.LoginScreen
import com.example.mind_mitra.auth.RoleSelectionScreen
import com.example.mind_mitra.auth.UserProfileScreen
import com.example.mind_mitra.auth.WelcomeScreen
import com.example.mind_mitra.user.UserHomeScreen
import com.example.mind_mitra.caregiver.CaregiverDashboardScreen

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.mind_mitra.network.RetrofitClient
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testBackendConnection()
        setContent {
            MindMitraApp()
        }
    }
    private fun testBackendConnection() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserMemories("pink-user")

                Log.d("MIND_MITRA_API", "Memories: ${response.memories}")

            } catch (e: Exception) {
                Log.e("MIND_MITRA_API", "API Error: ${e.message}", e)
            }
        }
    }
}

@Composable
fun MindMitraApp() {

    var currentScreen by remember {
        mutableStateOf("welcome")
    }

    var selectedRole by remember {
        mutableStateOf("")
    }

    var userName by remember {
        mutableStateOf("")
    }

    when (currentScreen) {

        "welcome" -> {

            WelcomeScreen(
                onGetStarted = {
                    currentScreen = "role_selection"
                }
            )
        }

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

        "login" -> {

            LoginScreen(
                role = selectedRole,

                onBack = {
                    currentScreen = "role_selection"
                },

                onLoginSuccess = {

                    if (selectedRole == "User") {
                        currentScreen = "user_profile"
                    } else {
                        currentScreen = "caregiver_home"
                    }
                }
            )
        }

        "user_profile" -> {

            UserProfileScreen(
                onProfileCompleted = { name ->

                    userName = name
                    currentScreen = "user_home"
                }
            )
        }

        "user_home" -> {

            UserHomeScreen(
                userName = userName,
            )
        }

        "caregiver_home" -> {

            CaregiverDashboardScreen(
                caregiverName = "Caregiver"
            )
        }
    }
}