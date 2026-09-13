package com.example.mind_mitra.music

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.R
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.data.MusicPreferenceCache
import com.example.mind_mitra.data.MusicPreferenceItem
import com.example.mind_mitra.ui.components.MindCard
import com.example.mind_mitra.ui.components.MindEmptyState
import com.example.mind_mitra.ui.components.MindLoadingState
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.theme.MindDarkText
import com.google.firebase.firestore.ListenerRegistration

fun openYouTubeUrl(context: android.content.Context, url: String) {
    val uri = Uri.parse(url)
    val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            setPackage("com.google.android.youtube")
        }
    }
    try {
        context.startActivity(appIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: Exception) {
            // Safe no-op if no handler is available.
        }
    }
}

fun openYouTubeSearch(context: android.content.Context, query: String) {
    val encoded = Uri.encode(query)
    val youtubeUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")
    val appIntent = Intent(Intent.ACTION_VIEW, youtubeUri).apply {
        setPackage("com.google.android.youtube")
    }
    try {
        context.startActivity(appIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, youtubeUri))
        } catch (_: Exception) {
            // Safe no-op if no handler is available.
        }
    }
}

fun openMusicPreference(context: android.content.Context, item: MusicPreferenceItem) {
    when {
        item.youtubeUrl.isNotBlank() -> openYouTubeUrl(context, item.youtubeUrl)
        item.searchQuery.isNotBlank() -> openYouTubeSearch(context, item.searchQuery)
        else -> openYouTubeSearch(context, item.label)
    }
}

private fun preferencesFromFirestore(prefs: Map<String, Any>?, context: android.content.Context): List<MusicPreferenceItem> {
    val raw = prefs?.get("musicPreferences")
    val loaded = MusicPreferenceItem.parseList(raw)
    if (loaded.isNotEmpty()) return loaded

    val legacy = (prefs?.get("favouriteMusic") as? String)?.trim()
    if (!legacy.isNullOrEmpty()) {
        return listOf(
            MusicPreferenceItem(
                id = "legacy_favourite",
                label = context.getString(R.string.music_favourite),
                searchQuery = legacy,
                category = "favourite_songs"
            )
        )
    }
    return emptyList()
}

@Composable
fun MusicScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val userId = AuthRepository.getCurrentUserId()
    var preferences by remember { mutableStateOf<List<MusicPreferenceItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }
    var playingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId == null) {
            preferences = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        preferences = MusicPreferenceCache.load(context, userId)
        isLoading = preferences.isEmpty()
        isOffline = false
    }

    DisposableEffect(userId) {
        if (userId == null) {
            onDispose { }
        } else {
            val listener: ListenerRegistration? = FirebaseRepository.listenToPreferences(
                userId = userId,
                onUpdate = { prefs ->
                    val loaded = preferencesFromFirestore(prefs, context)
                    preferences = loaded
                    MusicPreferenceCache.save(context, userId, loaded)
                    isLoading = false
                    isOffline = false
                },
                onError = {
                    isOffline = true
                    isLoading = false
                    FirebaseRepository.getPreferences(
                        userId = userId,
                        onSuccess = { prefs ->
                            val loaded = preferencesFromFirestore(prefs, context)
                            if (loaded.isNotEmpty() || preferences.isEmpty()) {
                                preferences = loaded
                                MusicPreferenceCache.save(context, userId, loaded)
                            }
                            isLoading = false
                        },
                        onError = { isLoading = false }
                    )
                }
            )
            onDispose { listener?.remove() }
        }
    }

    MindScreen(onBack = onBack) {
        MindSectionHeader(
            title = stringResource(R.string.music_title),
            subtitle = stringResource(R.string.music_subtitle)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isOffline && preferences.isNotEmpty()) {
            Text(
                text = stringResource(R.string.offline_message),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MindDarkText
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isLoading && preferences.isEmpty()) {
            MindLoadingState(stringResource(R.string.loading))
        } else if (preferences.isEmpty()) {
            MindEmptyState(
                title = stringResource(R.string.music_no_preferences_title),
                body = stringResource(R.string.music_no_preferences_body)
            )
        } else {
            preferences.forEach { item ->
                MusicPreferenceCard(
                    item = item,
                    isPlaying = playingId == item.id,
                    onPlay = {
                        playingId = item.id
                        openMusicPreference(context, item)
                        playingId = null
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MusicPreferenceCard(
    item: MusicPreferenceItem,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    MindCard(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            item.label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MindDarkText
        )
        if (item.searchQuery.isNotBlank() && item.youtubeUrl.isBlank() &&
            item.searchQuery != item.label
        ) {
            Text(
                item.searchQuery,
                fontSize = 18.sp,
                color = MindDarkText,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        MindPrimaryButton(
            text = if (isPlaying) {
                stringResource(R.string.music_opening)
            } else {
                stringResource(R.string.music_play)
            },
            onClick = onPlay
        )
    }
}
