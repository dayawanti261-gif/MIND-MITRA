package com.example.mind_mitra.auth

import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

private val DeepTeal = Color(0xFF146C68)
private val WarmWhite = Color(0xFFF9FBFA)
private val DarkText = Color(0xFF183331)
private val SecondaryText = Color(0xFF61716F)

@Composable
fun UserProfileScreen(
    onProfileCompleted: (String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var connectionPin by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Let's set up your profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "This helps MIND MITRA personalize your experience.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(32.dp))


        // NAME

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("What should we call you?")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )


        Spacer(modifier = Modifier.height(16.dp))


        // CONNECTION PIN

        OutlinedTextField(
            value = connectionPin,
            onValueChange = {

                // Only allow numbers
                if (it.all { char -> char.isDigit() } && it.length <= 6) {
                    connectionPin = it
                    errorMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Create 6-digit caregiver PIN")
            },
            placeholder = {
                Text("Example: 482615")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )


        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Share this PIN with your trusted caregiver.",
            fontSize = 13.sp,
            color = SecondaryText
        )


        Spacer(modifier = Modifier.height(12.dp))


        if (errorMessage.isNotEmpty()) {

            Text(
                text = errorMessage,
                color = Color(0xFFB3261E),
                fontSize = 14.sp
            )
        }


        Spacer(modifier = Modifier.height(24.dp))


        // CONTINUE BUTTON

        Button(
            onClick = {

                if (name.trim().isEmpty()) {

                    errorMessage = "Please enter your name."

                } else if (connectionPin.length != 6) {

                    errorMessage =
                        "Please create a 6-digit PIN."

                } else {

                    val userId =
                        AuthRepository.getCurrentUserId()

                    if (userId == null) {

                        errorMessage =
                            "User session not found. Please log in again."

                    } else {

                        val email =
                            AuthRepository.getCurrentUserEmail()

                        if (email == null) {

                            errorMessage =
                                "User email not found. Please log in again."

                        } else {

                            FirebaseRepository.saveUserProfile(
                                userId = userId,
                                name = name.trim(),
                                language = "Hindi",
                                email = email,
                                connectionPin = connectionPin,
                                onSuccess = {

                                    onProfileCompleted(
                                        name.trim()
                                    )
                                },
                                onError = { exception ->

                                    errorMessage =
                                        exception.message
                                            ?: "Failed to save profile. Please try again."
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
        ) {

            Text(
                text = "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}