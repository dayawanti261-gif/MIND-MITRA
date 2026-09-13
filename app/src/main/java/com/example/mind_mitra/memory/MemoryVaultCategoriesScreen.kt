package com.example.mind_mitra.memory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mind_mitra.R
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.MemoryCategories
import com.example.mind_mitra.network.MemoryData
import com.example.mind_mitra.network.RetrofitClient
import com.example.mind_mitra.ui.components.MindCard
import com.example.mind_mitra.ui.components.MindEmptyState
import com.example.mind_mitra.ui.components.MindLoadingState
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.components.MindSecondaryButton
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindDeepTeal
import com.example.mind_mitra.ui.theme.MindSecondaryText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Composable
fun MemoryVaultCategoriesScreen(
    onBack: (() -> Unit)? = null,
    patientUserId: String? = null
) {
    var memories by remember { mutableStateOf<List<MemoryData>>(emptyList()) }
    var photoUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedMemory by remember { mutableStateOf<MemoryData?>(null) }
    var memoryToDelete by remember { mutableStateOf<MemoryData?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ownerUserId = patientUserId ?: AuthRepository.getCurrentUserId()

    suspend fun loadMemories() {
        val userId = ownerUserId ?: return
        isLoading = true
        try {
            val loaded = withContext(Dispatchers.IO) {
                val response = RetrofitClient.apiService.getUserMemories(userId)
                val urls = mutableMapOf<String, String>()
                for (memory in response.memories) {
                    val url = memory.photo_url
                    if (!url.isNullOrEmpty()) {
                        urls[memory.id] = url
                    } else if (!memory.photo_path.isNullOrEmpty()) {
                        try {
                            urls[memory.id] = RetrofitClient.apiService
                                .getPhotoUrl(memory.photo_path).signed_url
                        } catch (_: Exception) {
                        }
                    }
                }
                Pair(response.memories, urls)
            }
            memories = loaded.first
            photoUrls = loaded.second
        } catch (_: Exception) {
            memories = emptyList()
            photoUrls = emptyMap()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(patientUserId) {
        loadMemories()
    }

    fun confirmDelete(memory: MemoryData) {
        val userId = ownerUserId
        if (userId == null) {
            deleteError = context.getString(R.string.delete_memory_error)
            return
        }
        scope.launch {
            isDeleting = true
            deleteError = null
            try {
                RetrofitClient.apiService.deleteMemory(userId, memory.id)
                memories = memories.filter { it.id != memory.id }
                if (selectedMemory?.id == memory.id) selectedMemory = null
                memoryToDelete = null
            } catch (e: HttpException) {
                deleteError = context.getString(R.string.delete_memory_error)
            } catch (_: Exception) {
                deleteError = context.getString(R.string.delete_memory_error)
            } finally {
                isDeleting = false
            }
        }
    }

    if (memoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) memoryToDelete = null },
            title = { Text(stringResource(R.string.delete_memory_confirm)) },
            text = { Text(stringResource(R.string.delete_memory_body)) },
            confirmButton = {
                TextButton(
                    onClick = { memoryToDelete?.let { confirmDelete(it) } },
                    enabled = !isDeleting
                ) {
                    Text(
                        stringResource(R.string.delete_memory),
                        color = MindDeepTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { memoryToDelete = null },
                    enabled = !isDeleting
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    MindScreen(onBack = when {
        selectedMemory != null -> ({ selectedMemory = null })
        selectedCategory != null -> ({ selectedCategory = null })
        else -> onBack
    }) {
        MindSectionHeader(
            title = stringResource(R.string.memory_vault_title),
            subtitle = stringResource(R.string.memory_vault_subtitle)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (!deleteError.isNullOrBlank()) {
            Text(
                text = deleteError!!,
                color = MindDeepTeal,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (isLoading) {
            MindLoadingState(stringResource(R.string.loading))
        } else when {
            selectedMemory != null -> {
                MemoryDetailView(
                    memory = selectedMemory!!,
                    photoUrl = photoUrls[selectedMemory!!.id],
                    isDeleting = isDeleting,
                    onDelete = { memoryToDelete = selectedMemory }
                )
            }
            selectedCategory == null -> {
                MemoryCategories.ALL.forEach { category ->
                    val count = memories.count {
                        MemoryCategories.normalize(it.category) == category
                    }
                    MindCard(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = stringResource(MemoryCategories.labelRes(category)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MindDarkText
                        )
                        Text(
                            text = stringResource(R.string.memory_count, count),
                            fontSize = 16.sp,
                            color = MindSecondaryText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            else -> {
                val filtered = memories.filter {
                    MemoryCategories.normalize(it.category) == selectedCategory
                }
                if (filtered.isEmpty()) {
                    MindEmptyState(
                        title = stringResource(R.string.no_memories_title),
                        body = stringResource(R.string.no_memories_body)
                    )
                } else {
                    filtered.forEach { memory ->
                        MemoryItemCard(
                            memory = memory,
                            photoUrl = photoUrls[memory.id],
                            onOpen = { selectedMemory = memory },
                            onDelete = { memoryToDelete = memory }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryDetailView(
    memory: MemoryData,
    photoUrl: String?,
    isDeleting: Boolean,
    onDelete: () -> Unit
) {
    MindCard {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = memory.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            memory.title ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MindDarkText
        )
        if (!memory.description.isNullOrBlank()) {
            Text(
                memory.description,
                fontSize = 17.sp,
                color = MindSecondaryText,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        MindSecondaryButton(
            text = stringResource(R.string.delete_memory),
            onClick = onDelete,
            enabled = !isDeleting
        )
    }
}

@Composable
private fun MemoryItemCard(
    memory: MemoryData,
    photoUrl: String?,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    MindCard(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen() }
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = memory.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    memory.title ?: "",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MindDarkText
                )
                if (!memory.description.isNullOrBlank()) {
                    Text(
                        memory.description,
                        fontSize = 16.sp,
                        color = MindSecondaryText,
                        modifier = Modifier.padding(top = 6.dp),
                        maxLines = 2
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_memory),
                    tint = MindDeepTeal
                )
            }
        }
    }
}
