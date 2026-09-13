package com.example.mind_mitra.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mind_mitra.R
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.network.LinkPatientRequest
import com.example.mind_mitra.network.LinkPatientHelper
import com.example.mind_mitra.network.RetrofitClient
import com.example.mind_mitra.ui.components.MindLargeTextField
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreenTitle
import com.example.mind_mitra.ui.components.MindStatusBanner
import com.example.mind_mitra.ui.theme.MindWarmWhite
import kotlinx.coroutines.launch

@Composable
fun ConnectPatientScreen(
    onConnected: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindWarmWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        MindScreenTitle(
            title = stringResource(R.string.connect_patient_title),
            subtitle = stringResource(R.string.connect_patient_subtitle)
        )

        Spacer(modifier = Modifier.height(28.dp))

        MindLargeTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
                isSuccess = false
            },
            label = stringResource(R.string.patient_email_label),
            placeholder = stringResource(R.string.patient_email_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MindLargeTextField(
            value = pin,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 6) {
                    pin = it
                    message = ""
                    isSuccess = false
                }
            },
            label = stringResource(R.string.connection_pin_label),
            placeholder = stringResource(R.string.connection_pin_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        MindPrimaryButton(
            text = if (isLoading) stringResource(R.string.connecting) else stringResource(R.string.connect_patient_button),
            enabled = !isLoading,
            onClick = {
                if (email.trim().isEmpty()) {
                    isSuccess = false
                    message = context.getString(R.string.error_enter_email)
                    return@MindPrimaryButton
                }
                if (pin.length != 6) {
                    isSuccess = false
                    message = context.getString(R.string.error_enter_pin)
                    return@MindPrimaryButton
                }
                val caregiverId = AuthRepository.getCurrentUserId()
                if (caregiverId == null) {
                    isSuccess = false
                    message = context.getString(R.string.error_session_not_found)
                    return@MindPrimaryButton
                }

                isLoading = true
                message = ""
                isSuccess = false

                scope.launch {
                    try {
                        RetrofitClient.apiService.linkPatient(
                            LinkPatientRequest(
                                patient_email = email.trim().lowercase(),
                                connection_pin = pin
                            )
                        )
                        isSuccess = true
                        message = context.getString(R.string.connect_success)
                        onConnected()
                    } catch (e: Exception) {
                        isSuccess = false
                        message = LinkPatientHelper.mapError(
                            throwable = e,
                            fallbackPatientNotFound = context.getString(R.string.error_patient_not_found),
                            fallbackInvalidPin = context.getString(R.string.error_invalid_pin),
                            fallbackNetwork = context.getString(R.string.error_network),
                            fallbackServer = context.getString(R.string.error_server)
                        )
                    } finally {
                        isLoading = false
                    }
                }
            }
        )

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            MindStatusBanner(text = message, isError = !isSuccess)
        }
    }
}
