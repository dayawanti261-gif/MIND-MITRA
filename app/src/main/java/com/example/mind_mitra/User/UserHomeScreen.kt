package com.example.mind_mitra.user

import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.ReminderItem
import com.example.mind_mitra.data.RoutineItem
import com.example.mind_mitra.games.CognitiveGameScreen
import com.example.mind_mitra.games.GameType
import com.example.mind_mitra.games.GamesHubScreen
import com.example.mind_mitra.games.ProgressRewardsScreen
import com.example.mind_mitra.music.MusicScreen
import com.example.mind_mitra.locale.LocaleHelper
import com.example.mind_mitra.memory.MemoryVaultCategoriesScreen
import com.example.mind_mitra.notifications.RequestNotificationPermissionIfNeeded
import com.example.mind_mitra.notifications.ReminderScheduler
import com.example.mind_mitra.network.AgentChatRequest
import androidx.compose.ui.res.stringResource
import com.example.mind_mitra.R
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mind_mitra.network.MemoryData
import com.example.mind_mitra.network.RetrofitClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

private val WarmWhite = Color(0xFFF9FBFA)
private val DarkText = Color.Black
private val SecondaryText = Color.Black
private val DeepTeal = Color(0xFF146C68)
private val SoftMint = Color(0xFFE8F5F2)
private val SoftCream = Color(0xFFF4F0E7)
private val SoftGold = Color(0xFFF4E8C7)
private val SoftBlue = Color(0xFFE5EEF7)
private val SoftLavender = Color(0xFFEDE8F5)

@Composable
fun UserHomeScreen(
    userName: String,
    onLogout: () -> Unit = {},
    onChangeLanguage: () -> Unit = {}
) {
    val viewModel: UserHomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val displayName = userName.ifBlank { uiState.userName.ifBlank { "Friend" } }

    RequestNotificationPermissionIfNeeded()

    var selectedTab by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf("main") }

    LaunchedEffect(uiState.reminders) {
        if (uiState.reminders.isNotEmpty() && AuthRepository.getCurrentUserId() != null) {
            viewModel.scheduleAlarms(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.ensureDataLoaded()
        viewModel.loadCachedRoutines(context)
    }

    LaunchedEffect(uiState.routines) {
        viewModel.cacheRoutines(context)
    }

    LaunchedEffect(currentPage) {
        if (currentPage == "main" || currentPage == "routine") {
            viewModel.refreshRoutines()
        }
    }
    var selectedGame by remember { mutableStateOf<GameType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {

        // This area takes all space above the bottom navigation.
        // This is the correct place to use weight.
        Column(
            modifier = Modifier.weight(1f)
        ) {

            when (currentPage) {

                "main" -> {

                    when (selectedTab) {

                        0 -> HomeContent(
                            userName = displayName,
                            routines = uiState.routines,
                            reminders = uiState.reminders,
                            isOffline = uiState.isOffline,
                            progressStats = uiState.progressStats,
                            onOpenGames = { selectedTab = 1 },
                            onOpenRoutine = {
                                currentPage = "routine"
                            },
                            onOpenMemories = {
                                currentPage = "memories"
                            },
                            onOpenMusic = {
                                currentPage = "music"
                            },
                            onOpenTalk = {
                                currentPage = "talk"
                            },
                            onOpenProgress = {
                                currentPage = "progress"
                            }
                        )

                        1 -> GamesHubScreen(
                            onPlayGame = { game ->
                                selectedGame = game
                                currentPage = "cognitive_game"
                            }
                        )

                        2 -> MemoryVaultCategoriesScreen()

                        3 -> MoreContent(
                            onChangeLanguage = onChangeLanguage,
                            onOpenRoutine = {
                                currentPage = "routine"
                            },
                            onOpenMusic = {
                                currentPage = "music"
                            },
                            onOpenTalk = {
                                currentPage = "talk"
                            },
                            onOpenProgress = {
                                currentPage = "progress"
                            },
                            onLogout = onLogout
                        )
                    }
                }

                "routine" -> RoutineScreen(
                    routines = uiState.routines,
                    isLoading = uiState.isLoading,
                    isOffline = uiState.isOffline,
                    onToggleRoutine = { id, completed ->
                        viewModel.toggleRoutine(id, completed)
                    },
                    onBack = {
                        currentPage = "main"
                    }
                )

                "cognitive_game" -> {
                    val game = selectedGame
                    if (game != null) {
                        CognitiveGameScreen(
                            gameType = game,
                            onBack = { currentPage = "main"; selectedTab = 1 }
                        )
                    } else {
                        GamesHubScreen(
                            onPlayGame = { g ->
                                selectedGame = g
                                currentPage = "cognitive_game"
                            }
                        )
                    }
                }

                "memories" -> MemoryVaultCategoriesScreen(
                    onBack = { currentPage = "main" }
                )

                "music" -> MusicScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "talk" -> TalkScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "progress" -> ProgressRewardsScreen(
                    onBack = { currentPage = "main" }
                )
            }
        }

        // Bottom navigation stays fixed.
        if (currentPage == "main") {

            BottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                }
            )
        }
    }
}


/* ================================================= */
/* HOME */
/* ================================================= */

@Composable
private fun HomeContent(
    userName: String,
    routines: List<RoutineItem>,
    reminders: List<ReminderItem>,
    isOffline: Boolean,
    progressStats: com.example.mind_mitra.data.ProgressStats,
    onOpenGames: () -> Unit,
    onOpenRoutine: () -> Unit,
    onOpenMemories: () -> Unit,
    onOpenMusic: () -> Unit,
    onOpenTalk: () -> Unit,
    onOpenProgress: () -> Unit
) {
    val today = com.example.mind_mitra.data.todayDateString()
    val todaysReminders = reminders.filter {
        it.enabled && !it.completedToday &&
            (it.date.isBlank() || it.date == today)
    }
    val nextReminder = todaysReminders.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 22.dp,
                vertical = 24.dp
            )
    ) {

        Text(
            text = "${stringResource(R.string.good_morning)}, $userName",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        if (isOffline) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.offline_message),
                fontSize = 18.sp,
                color = DarkText,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.todays_reminders),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (todaysReminders.isEmpty()) {
            Text(
                text = stringResource(R.string.no_reminders_today),
                fontSize = 18.sp,
                color = DarkText
            )
        } else {
            todaysReminders.take(3).forEach { reminder ->
                ReminderCard(
                    title = reminder.title,
                    time = reminder.time,
                    onOpenRoutine = onOpenRoutine
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.todays_routine),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        val activeRoutines = routines.filter { it.enabled }
        if (activeRoutines.isEmpty()) {
            Text(
                text = stringResource(R.string.no_routines_title),
                fontSize = 18.sp,
                color = DarkText
            )
        } else {
            activeRoutines.take(5).forEach { routine ->
                RoutinePreviewRow(
                    time = routine.time,
                    title = routine.title,
                    daysOfWeek = routine.daysOfWeek,
                    onOpenRoutine = onOpenRoutine
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        com.example.mind_mitra.ui.components.MindPrimaryButton(
            text = stringResource(R.string.talk_to_saathi),
            onClick = onOpenTalk
        )
        Spacer(modifier = Modifier.height(12.dp))
        com.example.mind_mitra.ui.components.MindSecondaryButton(
            text = stringResource(R.string.memory_vault_button),
            onClick = onOpenMemories
        )
        Spacer(modifier = Modifier.height(12.dp))
        com.example.mind_mitra.ui.components.MindSecondaryButton(
            text = stringResource(R.string.games_button),
            onClick = onOpenGames
        )
        Spacer(modifier = Modifier.height(12.dp))
        com.example.mind_mitra.ui.components.MindSecondaryButton(
            text = stringResource(R.string.progress_button),
            onClick = onOpenProgress
        )
        Spacer(modifier = Modifier.height(12.dp))
        com.example.mind_mitra.ui.components.MindSecondaryButton(
            text = stringResource(R.string.music_button),
            onClick = onOpenMusic
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


/* ================================================= */
/* REMINDER */
/* ================================================= */

@Composable
private fun RoutinePreviewRow(
    time: String,
    title: String,
    daysOfWeek: String,
    onOpenRoutine: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftMint,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onOpenRoutine() }
            .padding(20.dp)
    ) {
        Text(
            text = time,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        if (daysOfWeek.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = daysOfWeek,
                fontSize = 16.sp,
                color = DarkText
            )
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    time: String,
    onOpenRoutine: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftMint,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {

        Text(
            text = stringResource(R.string.next_up),
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = time,
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onOpenRoutine,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
        ) {

            Text(
                text = stringResource(R.string.view_routine),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}


/* ================================================= */
/* GAME CARD */
/* ================================================= */

@Composable
private fun GameCard(
    title: String,
    subtitle: String,
    modifier: Modifier,
    onPlay: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .background(
                color = SoftCream,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {

            Text("Play", fontSize = 16.sp)
        }
    }
}


/* ================================================= */
/* SCHEDULE */
/* ================================================= */

@Composable
private fun SchedulePreview(
    routines: List<RoutineItem>,
    onOpenRoutine: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Today's Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = onOpenRoutine
            ) {

                Text(
                    text = "View all",
                    color = DeepTeal
                )
            }
        }

        if (routines.isEmpty()) {
            Text(
                text = "No routine activities yet. Your caregiver can add them.",
                fontSize = 15.sp,
                color = SecondaryText,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            routines.take(5).forEach { routine ->
                ScheduleItem(
                    time = routine.time,
                    title = routine.title,
                    completed = routine.completed
                )
            }
        }
    }
}


@Composable
private fun ScheduleItem(
    time: String,
    title: String,
    completed: Boolean = false
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = time,
            modifier = Modifier.width(88.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTeal
        )

        Text(
            text = title,
            fontSize = 16.sp,
            color = DarkText
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        if (completed) {

            Text(
                text = "Done",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepTeal
            )
        }
    }
}


/* ================================================= */
/* MEMORY CARD */
/* ================================================= */

@Composable
private fun MemoryCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftMint,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = "Memory Vault",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Family photos, special moments and memories",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {

            Text("Open Memories")
        }
    }
}


/* ================================================= */
/* MUSIC CARD */
/* ================================================= */

@Composable
private fun MusicCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftCream,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = "Music & Rewards",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enjoy familiar music and see your achievements.",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
        ) {

            Text("Open Music & Rewards")
        }
    }
}


/* ================================================= */
/* TALK CARD */
/* ================================================= */

@Composable
private fun TalkCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftBlue,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = "Talk to MIND MITRA",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ask for help, reminders, memories or activities.",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
        ) {

            Text("Talk to MIND MITRA")
        }
    }
}


/* ================================================= */
/* PROGRESS CARD */
/* ================================================= */

@Composable
private fun ProgressCard(
    summary: String,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoftLavender,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = "Progress & Rewards",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = summary,
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {

            Text("View Progress")
        }
    }
}


/* ================================================= */
/* GAMES */
/* ================================================= */

@Composable
private fun GamesContent(
    onPlayMatching: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {

        Text(
            text = "Cognitive Games",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose an activity to exercise memory and attention.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        LargeGameCard(
            title = "Family Memory Matching",
            description = "Match familiar family photographs.",
            onPlay = onPlayMatching,
            enabled = true
        )

        LargeGameCard(
            title = "Pattern Recognition",
            description = "Find what comes next in the pattern.",
            enabled = false
        )

        LargeGameCard(
            title = "Personal Memory Recall",
            description = "Answer questions about meaningful memories.",
            enabled = false
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun LargeGameCard(
    title: String,
    description: String,
    onPlay: () -> Unit = {},
    enabled: Boolean = true
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .background(
                color = SoftMint,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onPlay,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
        ) {
            Text(if (enabled) "Play" else "Coming soon")
        }
    }
}


/* ================================================= */
/* MEMORY VAULT */
/* ================================================= */

data class MemoryCategory(
    val title: String,
    val description: String
)


@Composable
private fun MemoryVaultScreen(
    onBack: () -> Unit
) {

    var memories by remember {
        mutableStateOf<List<MemoryData>>(emptyList())
    }

    var photoUrls by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var selectedCategory by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        val userId = AuthRepository.getCurrentUserId()

        if (userId == null) {
            errorMessage = "User session not found."
            isLoading = false
        } else {
            try {
                val response =
                    RetrofitClient.apiService.getUserMemories(userId)
                Log.d("MEMORY_DEBUG", "Fetched ${response.memories.size} memories")
                memories = response.memories

                // Prefer photo_url from the memories API; fall back to signed-url.
                val urls = mutableMapOf<String, String>()
                for (memory in response.memories) {
                    val existing = memory.photo_url
                    if (!existing.isNullOrEmpty()) {
                        urls[memory.id] = existing
                    } else if (!memory.photo_path.isNullOrEmpty()) {
                        try {
                            val photoResponse =
                                RetrofitClient.apiService.getPhotoUrl(memory.photo_path)
                            urls[memory.id] = photoResponse.signed_url
                        } catch (_: Exception) {
                            // Ignore failed photo URL
                        }
                    }
                }

                photoUrls = urls
                isLoading = false

            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    val categories = listOf(
        MemoryCategory(
            "Family",
            "Photos and memories of family members"
        ),
        MemoryCategory(
            "Childhood",
            "Special memories from earlier years"
        ),
        MemoryCategory(
            "Important Events",
            "Birthdays, celebrations and special occasions"
        ),
        MemoryCategory(
            "Places",
            "Meaningful places and old photographs"
        ),
        MemoryCategory(
            "People",
            "Important people and familiar faces"
        ),
        MemoryCategory(
            "Voice Messages",
            "Messages shared by family"
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 22.dp,
                    vertical = 8.dp
                )
        ) {

            TextButton(onClick = onBack) {
                Text(
                    text = "← Back",
                    color = DeepTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Memory Vault",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A collection of familiar people, places and special memories.",
                fontSize = 16.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // NEW: surface load failures instead of silently showing an
            // empty list — this is what was hiding the 401 from the
            // backend's new auth requirement.
            if (errorMessage != null) {
                Text(
                    text = "Couldn't load memories: $errorMessage",
                    fontSize = 14.sp,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isLoading) {

            Text(
                text = "Loading memories...",
                modifier = Modifier.padding(horizontal = 22.dp),
                fontSize = 16.sp,
                color = SecondaryText
            )

        } else if (selectedCategory == null) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 22.dp,
                    end = 22.dp,
                    bottom = 22.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(categories) { category ->
                    MemoryCategoryCard(
                        category = category,
                        onClick = {
                            selectedCategory = category.title
                        }
                    )
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
            ) {

                TextButton(
                    onClick = {
                        selectedCategory = null
                    }
                ) {
                    Text(
                        text = "← Back to Memory Vault",
                        color = DeepTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = selectedCategory!!,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                val categoryMemories = memories.filter {
                    it.category?.equals(
                        selectedCategory,
                        ignoreCase = true
                    ) == true
                }

                if (categoryMemories.isEmpty()) {

                    Text(
                        text = "No memories found in this category.",
                        fontSize = 16.sp,
                        color = SecondaryText
                    )

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(categoryMemories) { memory ->

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    val imageUrl =
                                        memory.photo_url
                                            ?: photoUrls[memory.id]

                                    if (!imageUrl.isNullOrEmpty()) {

                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = memory.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(
                                            modifier = Modifier.height(12.dp)
                                        )
                                    }

                                    Text(
                                        text = memory.title
                                            ?: "Untitled Memory",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkText
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = memory.description ?: "",
                                        fontSize = 14.sp,
                                        color = SecondaryText
                                    )

                                    if (!memory.people.isNullOrEmpty()) {

                                        Spacer(
                                            modifier = Modifier.height(6.dp)
                                        )

                                        Text(
                                            text = "People: ${memory.people.joinToString(", ")}",
                                            fontSize = 14.sp,
                                            color = SecondaryText
                                        )
                                    }

                                    if (!memory.place.isNullOrEmpty()) {

                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )

                                        Text(
                                            text = "Place: ${memory.place}",
                                            fontSize = 14.sp,
                                            color = SecondaryText
                                        )
                                    }

                                    if (memory.year != null) {

                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )

                                        Text(
                                            text = "Year: ${memory.year}",
                                            fontSize = 14.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun MemoryCategoryCard(
    category: MemoryCategory,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = category.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = category.description,
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "View →",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepTeal
            )
        }
    }
}


/* ================================================= */
/* ROUTINE */
/* ================================================= */

@Composable
private fun RoutineScreen(
    routines: List<RoutineItem>,
    isLoading: Boolean,
    isOffline: Boolean,
    onToggleRoutine: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    com.example.mind_mitra.ui.components.MindScreen(onBack = onBack) {
        com.example.mind_mitra.ui.components.MindSectionHeader(
            title = stringResource(R.string.daily_routine),
            subtitle = stringResource(R.string.routine_subtitle)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isOffline) {
            Text(
                text = stringResource(R.string.offline_message),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isLoading && routines.isEmpty()) {
            com.example.mind_mitra.ui.components.MindLoadingState(
                stringResource(R.string.loading)
            )
        } else if (routines.isEmpty()) {
            com.example.mind_mitra.ui.components.MindEmptyState(
                title = stringResource(R.string.no_routines_title),
                body = stringResource(R.string.no_routines_body)
            )
        } else {
            routines.filter { it.enabled }.forEach { routine ->
                RoutineCard(
                    time = routine.time,
                    title = routine.title,
                    daysOfWeek = routine.daysOfWeek,
                    reminderNote = routine.reminderNote,
                    description = if (routine.completed) {
                        stringResource(R.string.routine_completed_today)
                    } else {
                        stringResource(R.string.routine_tap_to_complete)
                    },
                    completed = routine.completed,
                    onToggle = {
                        onToggleRoutine(routine.id, routine.completed)
                    }
                )
            }
        }
    }
}


@Composable
private fun RoutineCard(
    time: String,
    title: String,
    daysOfWeek: String = "",
    reminderNote: String = "",
    description: String,
    completed: Boolean = false,
    onToggle: () -> Unit = {}
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = time,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            if (daysOfWeek.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = daysOfWeek,
                    fontSize = 16.sp,
                    color = DarkText
                )
            }

            if (reminderNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminderNote,
                    fontSize = 16.sp,
                    color = DarkText
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = DarkText
            )

            if (completed) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
            }
        }
    }
}


/* ================================================= */
/* MUSIC & REWARDS */
/* ================================================= */

@Composable
private fun MusicRewardsScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {

            Text(
                text = "← Back",
                color = DeepTeal,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Music & Rewards",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enjoy familiar music and celebrate your progress.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        MusicOption(
            "Favourite Music",
            "Music selected according to your preferences."
        )

        MusicOption(
            "Calm Music",
            "A gentle listening option for a quiet moment."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Rewards",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        RewardCard(
            "Memory Explorer",
            "Complete memory activities."
        )

        RewardCard(
            "Focus Builder",
            "Keep practicing cognitive activities."
        )

        RewardCard(
            "Daily Routine",
            "Complete your planned activities."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun MusicOption(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftCream
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {}
            ) {

                Text("Play")
            }
        }
    }
}


@Composable
private fun RewardCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftGold
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "Achievement",
                fontSize = 13.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SecondaryText
            )
        }
    }
}


/* ================================================= */
/* TALK */
/* ================================================= */

private data class ChatMessage(val text: String, val fromUser: Boolean)

@Composable
private fun TalkScreen(
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val greeting = stringResource(R.string.talk_greeting)
    var messages by remember {
        mutableStateOf(listOf(ChatMessage(greeting, fromUser = false)))
    }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun sendMessage(message: String) {
        if (message.isBlank() || isLoading) return
        if (AuthRepository.getCurrentUserId() == null) {
            messages = messages + ChatMessage(
                context.getString(R.string.talk_auth_error),
                fromUser = false
            )
            return
        }
        messages = messages + ChatMessage(message, fromUser = true)
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isLoading = true }
            try {
                val response = RetrofitClient.apiService.chatWithAgent(
                    AgentChatRequest(
                        message = message,
                        language = LocaleHelper.getSavedLanguage(context)
                    )
                )
                val reply = response.message?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.talk_greeting)
                withContext(Dispatchers.Main) {
                    messages = messages + ChatMessage(reply, fromUser = false)
                    if (message == input.trim()) input = ""
                }
            } catch (e: retrofit2.HttpException) {
                val errorText = when (e.code()) {
                    503 -> context.getString(R.string.talk_ai_unavailable)
                    401 -> context.getString(R.string.talk_auth_error)
                    else -> context.getString(R.string.talk_error)
                }
                withContext(Dispatchers.Main) {
                    messages = messages + ChatMessage(errorText, fromUser = false)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    messages = messages + ChatMessage(
                        context.getString(R.string.talk_error),
                        fromUser = false
                    )
                }
            } finally {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    com.example.mind_mitra.ui.components.MindScreen(onBack = onBack) {
        com.example.mind_mitra.ui.components.MindSectionHeader(
            title = stringResource(R.string.talk_title),
            subtitle = stringResource(R.string.talk_subtitle)
        )
        Spacer(modifier = Modifier.height(16.dp))

        messages.forEach { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.fromUser) SoftMint else SoftBlue
                )
            ) {
                Text(
                    msg.text,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 17.sp,
                    color = DarkText,
                    lineHeight = 24.sp
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = DeepTeal,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.talk_type_hint)) },
            singleLine = false,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        com.example.mind_mitra.ui.components.MindPrimaryButton(
            text = stringResource(R.string.talk_send),
            onClick = { sendMessage(input.trim()) },
            enabled = !isLoading && input.isNotBlank()
        )

        Spacer(modifier = Modifier.height(20.dp))

        val suggestionActivity = stringResource(R.string.talk_suggestion_activity)
        val suggestionGame = stringResource(R.string.talk_suggestion_game)
        val suggestionMemory = stringResource(R.string.talk_suggestion_memory)

        Text(
            stringResource(R.string.talk_try_asking),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(12.dp))
        SuggestionCard(suggestionActivity, onClick = { sendMessage(suggestionActivity) })
        SuggestionCard(suggestionGame, onClick = { sendMessage(suggestionGame) })
        SuggestionCard(suggestionMemory, onClick = { sendMessage(suggestionMemory) })
    }
}


@Composable
private fun SuggestionCard(
    text: String,
    onClick: () -> Unit = {}
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 15.sp,
            color = DarkText
        )
    }
}


/* ================================================= */
/* PROGRESS */
/* ================================================= */

@Composable
private fun ProgressScreen(
    stats: com.example.mind_mitra.data.ProgressStats,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {

            Text(
                text = "← Back",
                color = DeepTeal,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Progress & Rewards",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your activity and progress summary.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProgressItem(
            "Memory Game",
            "${stats.memoryGame}%"
        )

        ProgressItem(
            "Pattern Game",
            "${stats.patternGame}%"
        )

        ProgressItem(
            "Recall Game",
            "${stats.recallGame}%"
        )

        Spacer(modifier = Modifier.height(20.dp))

        SummaryCard(
            "Activities Completed",
            "${stats.activitiesCompleted}"
        )

        SummaryCard(
            "Routine Completion",
            "${stats.routineCompletion}%"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Milestones",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        RewardCard(
            "Memory Explorer",
            "Keep exploring meaningful memories."
        )

        RewardCard(
            "Focus Builder",
            "Keep practicing your activities."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun ProgressItem(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
        }
    }
}


@Composable
private fun SummaryCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftCream
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                color = DarkText
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
        }
    }
}


/* ================================================= */
/* MORE */
/* ================================================= */

@Composable
private fun MoreContent(
    onOpenRoutine: () -> Unit,
    onOpenMusic: () -> Unit,
    onOpenTalk: () -> Unit,
    onOpenProgress: () -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {

        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(20.dp))

        MoreButton(
            stringResource(R.string.daily_routine),
            onOpenRoutine
        )

        MoreButton(
            stringResource(R.string.music_rewards),
            onOpenMusic
        )

        MoreButton(
            stringResource(R.string.talk_title),
            onOpenTalk
        )

        MoreButton(
            stringResource(R.string.progress_title),
            onOpenProgress
        )

        MoreButton(
            title = stringResource(R.string.change_language),
            onClick = onChangeLanguage
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                AuthRepository.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                stringResource(R.string.logout),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepTeal
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun MoreButton(
    title: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {

            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
        }
    }
}


/* ================================================= */
/* BOTTOM NAVIGATION */
/* ================================================= */

@Composable
private fun BottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    val tabs = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_games),
        stringResource(R.string.nav_memories),
        stringResource(R.string.nav_more)
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {

        tabs.forEachIndexed { index, title ->

            NavigationBarItem(
                selected = selectedTab == index,

                onClick = {
                    onTabSelected(index)
                },

                icon = {

                    Text(
                        text = when (index) {
                            0 -> "⌂"
                            1 -> "▶"
                            2 -> "▣"
                            else -> "☰"
                        }
                    )
                },

                label = {
                    Text(title)
                }
            )
        }
    }
}