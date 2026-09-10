package com.example.mind_mitra.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val WarmWhite = Color(0xFFF9FBFA)
private val DarkText = Color(0xFF183331)
private val SecondaryText = Color(0xFF61716F)
private val DeepTeal = Color(0xFF146C68)
private val SoftMint = Color(0xFFE8F5F2)
private val SoftCream = Color(0xFFF4F0E7)
private val SoftGold = Color(0xFFF4E8C7)
private val SoftBlue = Color(0xFFE5EEF7)
private val SoftLavender = Color(0xFFEDE8F5)

@Composable
fun UserHomeScreen(userName: String) {

    var selectedTab by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf("main") }

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
                            userName = userName,
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

                        1 -> GamesContent()

                        2 -> MemoryVaultScreen(
                            onBack = {
                                selectedTab = 0
                            }
                        )

                        3 -> MoreContent(
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
                            }
                        )
                    }
                }

                "routine" -> RoutineScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "memories" -> MemoryVaultScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "music" -> MusicRewardsScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "talk" -> TalkScreen(
                    onBack = {
                        currentPage = "main"
                    }
                )

                "progress" -> ProgressScreen(
                    onBack = {
                        currentPage = "main"
                    }
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
    onOpenRoutine: () -> Unit,
    onOpenMemories: () -> Unit,
    onOpenMusic: () -> Unit,
    onOpenTalk: () -> Unit,
    onOpenProgress: () -> Unit
) {

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
            text = "Good morning, $userName",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Let's make today a good day.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TODAY'S REMINDER",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTeal
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReminderCard()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recommended for you",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            GameCard(
                title = "Memory Matching",
                subtitle = "Family photos",
                modifier = Modifier.weight(1f)
            )

            GameCard(
                title = "Pattern",
                subtitle = "Train your focus",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SchedulePreview(
            onOpenRoutine = onOpenRoutine
        )

        Spacer(modifier = Modifier.height(20.dp))

        MemoryCard(
            onClick = onOpenMemories
        )

        Spacer(modifier = Modifier.height(20.dp))

        MusicCard(
            onClick = onOpenMusic
        )

        Spacer(modifier = Modifier.height(20.dp))

        TalkCard(
            onClick = onOpenTalk
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProgressCard(
            onClick = onOpenProgress
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


/* ================================================= */
/* REMINDER */
/* ================================================= */

@Composable
private fun ReminderCard() {

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
            text = "Next up",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Cognitive Activity",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "10:00 AM  •  15 minutes",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {},
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
        ) {

            Text(
                text = "Start Activity",
                fontWeight = FontWeight.SemiBold
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
    modifier: Modifier
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
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {

            Text("Play")
        }
    }
}


/* ================================================= */
/* SCHEDULE */
/* ================================================= */

@Composable
private fun SchedulePreview(
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

        ScheduleItem("7:00 AM", "Wake up")

        ScheduleItem(
            "8:00 AM",
            "Breakfast",
            true
        )

        ScheduleItem(
            "10:00 AM",
            "Cognitive Activity"
        )

        ScheduleItem(
            "1:00 PM",
            "Lunch"
        )

        ScheduleItem(
            "4:00 PM",
            "Doctor Appointment"
        )

        ScheduleItem(
            "7:30 PM",
            "Dinner"
        )
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
            text = "See your activities, progress and achievements.",
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
private fun GamesContent() {

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
            "Family Memory Matching",
            "Match familiar family photographs."
        )

        LargeGameCard(
            "Pattern Recognition",
            "Find what comes next in the pattern."
        )

        LargeGameCard(
            "Personal Memory Recall",
            "Answer questions about meaningful memories."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun LargeGameCard(
    title: String,
    description: String
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

        Text(
            text = "Open Game →",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTeal
        )
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
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 22.dp,
                end = 22.dp,
                bottom = 22.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(categories) { category ->

                MemoryCategoryCard(category)
            }
        }
    }
}


@Composable
private fun MemoryCategoryCard(
    category: MemoryCategory
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            text = "Daily Routine",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your activities and reminders for today.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoutineCard(
            "7:00 AM",
            "Wake up",
            "Start your morning routine."
        )

        RoutineCard(
            "8:00 AM",
            "Breakfast",
            "Morning meal.",
            true
        )

        RoutineCard(
            "10:00 AM",
            "Cognitive Activity",
            "Spend a few minutes on a cognitive activity."
        )

        RoutineCard(
            "1:00 PM",
            "Lunch",
            "Afternoon meal."
        )

        RoutineCard(
            "4:00 PM",
            "Doctor Appointment",
            "Caregiver-entered appointment reminder."
        )

        RoutineCard(
            "7:30 PM",
            "Dinner",
            "Evening meal."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun RoutineCard(
    time: String,
    title: String,
    description: String,
    completed: Boolean = false
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

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
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

            if (completed) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepTeal
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

@Composable
private fun TalkScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "← Back",
                color = DeepTeal,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Talk to MIND MITRA",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You can ask for help, reminders, activities or memories.",
            fontSize = 16.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(45.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SoftBlue
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Voice interaction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Press the button when voice interaction is connected.",
                    fontSize = 14.sp,
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
                ) {

                    Text(
                        text = "🎙  Tap to Talk",
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Try asking:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        SuggestionCard("Show me my daughter's photo")

        SuggestionCard("What is my next activity?")

        SuggestionCard("Recommend a game")

        SuggestionCard("Play my favourite music")

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
private fun SuggestionCard(
    text: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
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
            "82%"
        )

        ProgressItem(
            "Pattern Game",
            "74%"
        )

        ProgressItem(
            "Recall Game",
            "79%"
        )

        Spacer(modifier = Modifier.height(20.dp))

        SummaryCard(
            "Activities Completed",
            "12"
        )

        SummaryCard(
            "Routine Completion",
            "85%"
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
    onOpenProgress: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {

        Text(
            text = "More",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(20.dp))

        MoreButton(
            "Daily Routine",
            onOpenRoutine
        )

        MoreButton(
            "Music & Rewards",
            onOpenMusic
        )

        MoreButton(
            "Talk to MIND MITRA",
            onOpenTalk
        )

        MoreButton(
            "Progress",
            onOpenProgress
        )

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
        "Home",
        "Games",
        "Memories",
        "More"
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