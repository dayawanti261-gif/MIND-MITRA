package com.example.mind_mitra.caregiver

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mind_mitra.R
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.data.MemoryCategories
import com.example.mind_mitra.network.MemoryRequest
import com.example.mind_mitra.network.PhotoUploadHelper
import com.example.mind_mitra.network.RetrofitClient
import com.example.mind_mitra.ui.components.MindLargeTextField
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.components.MindStatusBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var category by remember { mutableStateOf(MemoryCategories.FAMILY) }
    var expanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    MindScreen(onBack = onBack) {
        MindSectionHeader(
            title = stringResource(R.string.add_memory_title),
            subtitle = stringResource(R.string.add_memory_subtitle)
        )
        Spacer(modifier = Modifier.height(20.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = stringResource(MemoryCategories.labelRes(category)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MemoryCategories.ALL.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(stringResource(MemoryCategories.labelRes(cat))) },
                        onClick = { category = cat; expanded = false }
                    )
                }
            }
        }

        MindSecondaryPhotoButton(onClick = { picker.launch("image/*") })
        Spacer(modifier = Modifier.height(12.dp))

        MindLargeTextField(
            value = title,
            onValueChange = { title = it },
            label = stringResource(R.string.memory_title_label),
            placeholder = stringResource(R.string.memory_title_hint)
        )
        Spacer(modifier = Modifier.height(12.dp))

        MindLargeTextField(
            value = description,
            onValueChange = { description = it },
            label = stringResource(R.string.memory_desc_label),
            placeholder = stringResource(R.string.memory_desc_hint),
            singleLine = false
        )
        Spacer(modifier = Modifier.height(20.dp))

        MindPrimaryButton(
            text = if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_memory),
            enabled = !isSaving,
            onClick = {
                if (title.isBlank()) {
                    isError = true
                    message = context.getString(R.string.error_memory_title)
                    return@MindPrimaryButton
                }
                val caregiverId = AuthRepository.getCurrentUserId()
                if (caregiverId == null) {
                    isError = true
                    message = context.getString(R.string.error_session_not_found)
                    return@MindPrimaryButton
                }
                isSaving = true
                FirebaseRepository.getLinkedPatientId(
                    caregiverId = caregiverId,
                    onSuccess = { patientId ->
                        if (patientId == null) {
                            isSaving = false
                            isError = true
                            message = context.getString(R.string.error_no_patient)
                        } else {
                            scope.launch {
                                try {
                                    var photoPath: String? = null
                                    if (photoUri != null) {
                                        val upload = PhotoUploadHelper.uploadPhoto(
                                            context = context,
                                            userId = patientId,
                                            title = title.trim(),
                                            description = description.trim(),
                                            category = category,
                                            uri = photoUri!!
                                        )
                                        photoPath = upload.file_path
                                    }
                                    RetrofitClient.apiService.addMemory(
                                        MemoryRequest(
                                            user_id = patientId,
                                            title = title.trim(),
                                            description = description.trim(),
                                            category = category,
                                            photo_path = photoPath
                                        )
                                    )
                                    isError = false
                                    message = context.getString(R.string.memory_saved)
                                    onSaved()
                                } catch (e: Exception) {
                                    isError = true
                                    message = context.getString(R.string.error_server)
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    },
                    onError = {
                        isSaving = false
                        isError = true
                        message = context.getString(R.string.error_server)
                    }
                )
            }
        )

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            MindStatusBanner(text = message, isError = isError)
        }
    }
}

@Composable
private fun MindSecondaryPhotoButton(onClick: () -> Unit) {
    com.example.mind_mitra.ui.components.MindSecondaryButton(
        text = stringResource(R.string.add_photo),
        onClick = onClick
    )
}
