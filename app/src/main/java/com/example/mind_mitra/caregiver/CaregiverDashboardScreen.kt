package com.example.mind_mitra.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepTeal = Color(0xFF146C68)
private val WarmWhite = Color(0xFFF9FBFA)
private val SoftMint = Color(0xFFE8F5F2)
private val DarkText = Color(0xFF183331)
private val SecondaryText = Color(0xFF61716F)
private val LightCard = Color.White

@Composable
fun CaregiverDashboardScreen(
    caregiverName: String
) {

    var currentTab by remember { mutableStateOf("home") }
    var currentSection by remember { mutableStateOf("dashboard") }

    if (currentSection != "dashboard") {

        when (currentSection) {

            "profile" -> {
                CaregiverSectionScreen(
                    title = "Elderly Profile",
                    subtitle = "Manage the elderly user's personal information and preferences.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {
                    SectionInfoCard(
                        title = "Personal Information",
                        description = "Name, age and basic profile information."
                    )

                    SectionInfoCard(
                        title = "Family Information",
                        description = "Manage important family relationships."
                    )

                    SectionInfoCard(
                        title = "Personal Preferences",
                        description = "Favourite activities, music and interests."
                    )

                    FrontendActionButton(
                        text = "Edit Profile"
                    )
                }
            }

            "family" -> {
                CaregiverSectionScreen(
                    title = "Family",
                    subtitle = "Manage family members who are connected with the elderly user.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {
                    SectionInfoCard(
                        title = "Family Members",
                        description = "View and manage important family members."
                    )

                    FrontendActionButton(
                        text = "Add Family Member"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SectionInfoCard(
                        title = "Family Connection",
                        description = "Family can send greetings, photos, voice messages and short videos."
                    )
                }
            }

            "memories" -> {
                CaregiverSectionScreen(
                    title = "Memory Vault",
                    subtitle = "Organize meaningful memories for the elderly user.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    MemoryCategoryCard("Family Photos")
                    MemoryCategoryCard("Childhood Memories")
                    MemoryCategoryCard("Important Events")
                    MemoryCategoryCard("Places")
                    MemoryCategoryCard("People")
                    MemoryCategoryCard("Voice Messages")

                    Spacer(modifier = Modifier.height(8.dp))

                    FrontendActionButton(
                        text = "Add Memory"
                    )
                }
            }

            "routine" -> {
                CaregiverSectionScreen(
                    title = "Daily Routine",
                    subtitle = "Manage the user's daily schedule and reminders.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    RoutineItem(
                        time = "7:00 AM",
                        activity = "Wake Up"
                    )

                    RoutineItem(
                        time = "8:00 AM",
                        activity = "Breakfast"
                    )

                    RoutineItem(
                        time = "10:00 AM",
                        activity = "Cognitive Activity"
                    )

                    RoutineItem(
                        time = "1:00 PM",
                        activity = "Lunch"
                    )

                    RoutineItem(
                        time = "4:00 PM",
                        activity = "Walk"
                    )

                    RoutineItem(
                        time = "8:00 PM",
                        activity = "Dinner"
                    )
                }
            }

            "progress" -> {
                CaregiverSectionScreen(
                    title = "Progress & Activities",
                    subtitle = "Monitor weekly activities and overall engagement.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    ProgressCard(
                        title = "Memory Game",
                        value = "82%"
                    )

                    ProgressCard(
                        title = "Pattern Game",
                        value = "74%"
                    )

                    ProgressCard(
                        title = "Recall Game",
                        value = "79%"
                    )

                    ProgressCard(
                        title = "Activities Completed",
                        value = "12 activities"
                    )

                    ProgressCard(
                        title = "Routine Completion",
                        value = "85%"
                    )

                    ProgressCard(
                        title = "Engagement Trend",
                        value = "Improving"
                    )
                }
            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(
                text = "Caregiver Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Welcome, $caregiverName",
                fontSize = 14.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SoftMint
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Elderly User",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Manage the user's profile, memories, routine and preferences.",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FrontendActionButton(
                        text = "View Profile",
                        onClick = {
                            currentSection = "profile"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                QuickActionButton(
                    text = "Elderly Profile",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSection = "profile"
                    }
                )

                QuickActionButton(
                    text = "Family",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSection = "family"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                QuickActionButton(
                    text = "Memories",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSection = "memories"
                    }
                )

                QuickActionButton(
                    text = "Daily Routine",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentSection = "routine"
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Weekly Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightCard
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Activity Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SummaryRow(
                        title = "Games",
                        value = "View",
                        onClick = {
                            currentSection = "progress"
                        }
                    )

                    SummaryRow(
                        title = "Activities",
                        value = "View",
                        onClick = {
                            currentSection = "progress"
                        }
                    )

                    SummaryRow(
                        title = "Routine",
                        value = "View",
                        onClick = {
                            currentSection = "progress"
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FrontendActionButton(
                        text = "View Progress",
                        onClick = {
                            currentSection = "progress"
                        }
                    )
                }
            }
        }

        NavigationBar(
            containerColor = Color.White
        ) {

            NavigationBarItem(
                selected = currentTab == "home",
                onClick = {
                    currentTab = "home"
                    currentSection = "dashboard"
                },
                icon = {},
                label = {
                    Text("Home")
                }
            )

            NavigationBarItem(
                selected = currentTab == "manage",
                onClick = {
                    currentTab = "manage"
                    currentSection = "profile"
                },
                icon = {},
                label = {
                    Text("Manage")
                }
            )

            NavigationBarItem(
                selected = currentTab == "monitor",
                onClick = {
                    currentTab = "monitor"
                    currentSection = "progress"
                },
                icon = {},
                label = {
                    Text("Monitor")
                }
            )

            NavigationBarItem(
                selected = currentTab == "more",
                onClick = {
                    currentTab = "more"
                },
                icon = {},
                label = {
                    Text("More")
                }
            )
        }
    }
}

@Composable
private fun CaregiverSectionScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftMint,
                contentColor = DeepTeal
            )
        ) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        content()
    }
}

@Composable
private fun SectionInfoCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun MemoryCategoryCard(
    title: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )
    }
}

@Composable
private fun RoutineItem(
    time: String,
    activity: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = activity,
                fontSize = 14.sp,
                color = DarkText
            )
        }
    }
}

@Composable
private fun ProgressCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                fontSize = 13.sp,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun FrontendActionButton(
    text: String,
    onClick: () -> Unit = {}
) {

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepTeal,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftMint,
            contentColor = DeepTeal
        ),
        shape = RoundedCornerShape(14.dp)
    ) {

        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SummaryRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            fontSize = 13.sp,
            color = SecondaryText
        )

        Button(
            onClick = onClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = DeepTeal
            )
        ) {

            Text(
                text = value,
                fontSize = 12.sp
            )
        }
    }
}