package com.example.mind_mitra.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository

@Composable
fun ConnectPatientScreen(
    onConnected: () -> Unit
) {

    var email by remember {
        mutableStateOf("")
    }

    var pin by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Connect Patient",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter the patient's registered email and 6-digit PIN."
        )

        Spacer(modifier = Modifier.height(24.dp))


        // PATIENT EMAIL

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Patient Email")
            },
            singleLine = true
        )


        Spacer(modifier = Modifier.height(16.dp))


        // PATIENT PIN

        OutlinedTextField(
            value = pin,
            onValueChange = {

                if (it.all { char -> char.isDigit() } && it.length <= 6) {
                    pin = it
                    message = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("6-digit Connection PIN")
            },
            placeholder = {
                Text("Enter PIN")
            },
            singleLine = true
        )


        Spacer(modifier = Modifier.height(20.dp))


        // CONNECT

        Button(
            onClick = {

                if (email.trim().isEmpty()) {

                    message =
                        "Please enter the patient's email."

                    return@Button
                }

                if (pin.length != 6) {

                    message =
                        "Please enter the 6-digit PIN."

                    return@Button
                }


                val caregiverId =
                    AuthRepository.getCurrentUserId()

                if (caregiverId == null) {

                    message =
                        "Caregiver session not found."

                    return@Button
                }


                isLoading = true
                message = ""


                FirebaseRepository.findPatientByEmailAndPin(
                    email = email.trim(),
                    connectionPin = pin,

                    onSuccess = { patientId, _ ->

                        if (patientId == null) {

                            isLoading = false

                            message =
                                "Invalid patient email or PIN."

                        } else {

                            FirebaseRepository.linkCaregiverToPatient(
                                caregiverId = caregiverId,
                                patientId = patientId,

                                onSuccess = {

                                    isLoading = false

                                    onConnected()
                                },

                                onError = { exception ->

                                    isLoading = false

                                    message =
                                        exception.message
                                            ?: "Failed to connect patient."
                                }
                            )
                        }
                    },

                    onError = { exception ->

                        isLoading = false

                        message =
                            exception.message
                                ?: "Failed to find patient."
                    }
                )
            },

            modifier = Modifier.fillMaxWidth(),

            enabled = !isLoading
        ) {

            Text(
                if (isLoading)
                    "Connecting..."
                else
                    "Connect Patient"
            )
        }


        if (message.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
