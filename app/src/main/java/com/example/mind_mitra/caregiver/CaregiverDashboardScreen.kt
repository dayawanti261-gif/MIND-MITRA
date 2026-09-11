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

    /*
     * If a separate management screen is opened,
     * show that screen first.
     */
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
                        activity = "Walk / Activity"
                    )

                    RoutineItem(
                        time = "8:00 PM",
                        activity = "Dinner"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FrontendActionButton(
                        text = "Add Routine Activity"
                    )
                }
            }

            "tasks" -> {
                CaregiverSectionScreen(
                    title = "Tasks & Reminders",
                    subtitle = "Create and manage tasks for the elderly user's daily routine.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    TaskCard(
                        title = "Morning Routine",
                        description = "Complete morning activities."
                    )

                    TaskCard(
                        title = "Cognitive Activity",
                        description = "Complete today's recommended cognitive activity."
                    )

                    TaskCard(
                        title = "Family Connection",
                        description = "Spend some time connecting with family."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FrontendActionButton(
                        text = "Create New Task"
                    )
                }
            }

            "preferences" -> {
                CaregiverSectionScreen(
                    title = "Preferences",
                    subtitle = "Manage activities and content the elderly user enjoys.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    PreferenceCard(
                        title = "Favourite Music",
                        description = "Manage preferred music and songs."
                    )

                    PreferenceCard(
                        title = "Favourite Activities",
                        description = "Manage enjoyable cognitive and daily activities."
                    )

                    PreferenceCard(
                        title = "Favourite Memories",
                        description = "Highlight meaningful memories."
                    )

                    PreferenceCard(
                        title = "Language",
                        description = "English, Hindi, Assamese, Bengali and other supported languages."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FrontendActionButton(
                        text = "Edit Preferences"
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


    /*
     * MAIN CAREGIVER AREA
     */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {

        /*
         * CONTENT AREA
         */

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            when (currentTab) {

                "home" -> {
                    CaregiverHomeTab(
                        caregiverName = caregiverName,
                        onSectionSelected = {
                            currentSection = it
                        },
                        onMonitorSelected = {
                            currentTab = "monitor"
                        }
                    )
                }

                "manage" -> {
                    CaregiverManageTab(
                        onSectionSelected = {
                            currentSection = it
                        }
                    )
                }

                "monitor" -> {
                    CaregiverMonitorTab(
                        onSectionSelected = {
                            currentSection = it
                        }
                    )
                }

                "more" -> {
                    CaregiverMoreTab(
                        onSectionSelected = {
                            currentSection = it
                        }
                    )
                }
            }
        }


        /*
         * FIXED BOTTOM NAVIGATION
         */

        NavigationBar(
            containerColor = Color.White
        ) {

            NavigationBarItem(
                selected = currentTab == "home",
                onClick = {
                    currentTab = "home"
                },
                icon = {
                    Text(
                        text = "⌂",
                        fontSize = 22.sp
                    )
                },
                label = {
                    Text("Home")
                }
            )

            NavigationBarItem(
                selected = currentTab == "manage",
                onClick = {
                    currentTab = "manage"
                },
                icon = {
                    Text(
                        text = "●",
                        fontSize = 20.sp
                    )
                },
                label = {
                    Text("Manage")
                }
            )

            NavigationBarItem(
                selected = currentTab == "monitor",
                onClick = {
                    currentTab = "monitor"
                },
                icon = {
                    Text(
                        text = "▥",
                        fontSize = 21.sp
                    )
                },
                label = {
                    Text("Monitor")
                }
            )

            NavigationBarItem(
                selected = currentTab == "more",
                onClick = {
                    currentTab = "more"
                },
                icon = {
                    Text(
                        text = "•••",
                        fontSize = 18.sp
                    )
                },
                label = {
                    Text("More")
                }
            )
        }
    }
}


/* ===================================================
   HOME TAB
=================================================== */

@Composable
private fun CaregiverHomeTab(
    caregiverName: String,
    onSectionSelected: (String) -> Unit,
    onMonitorSelected: () -> Unit
) {

    Text(
        text = "Caregiver Dashboard",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Welcome, $caregiverName",
        fontSize = 16.sp,
        color = SecondaryText
    )

    Spacer(modifier = Modifier.height(22.dp))


    /*
     * ELDERLY USER CARD
     */

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Elderly User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Manage the user's profile, memories, routine and preferences.",
                fontSize = 15.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSectionSelected("profile")
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal
                )
            ) {

                Text(
                    text = "View Profile",
                    color = Color.White
                )
            }
        }
    }


    Spacer(modifier = Modifier.height(26.dp))


    /*
     * QUICK ACTIONS
     */

    Text(
        text = "Quick Actions",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(14.dp))

    DashboardButtonRow(
        firstTitle = "Elderly Profile",
        firstAction = {
            onSectionSelected("profile")
        },
        secondTitle = "Family",
        secondAction = {
            onSectionSelected("family")
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    DashboardButtonRow(
        firstTitle = "Memories",
        firstAction = {
            onSectionSelected("memories")
        },
        secondTitle = "Daily Routine",
        secondAction = {
            onSectionSelected("routine")
        }
    )


    Spacer(modifier = Modifier.height(26.dp))


    /*
     * WEEKLY OVERVIEW
     */

    Text(
        text = "Weekly Overview",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(14.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Activity Summary",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(12.dp))

            MonitorRow(
                title = "Games",
                value = "View"
            )

            MonitorRow(
                title = "Activities",
                value = "View"
            )

            MonitorRow(
                title = "Routine",
                value = "View"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onMonitorSelected,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal
                )
            ) {

                Text(
                    text = "View Progress",
                    color = Color.White
                )
            }
        }
    }


    Spacer(modifier = Modifier.height(20.dp))
}


/* ===================================================
   MANAGE TAB
=================================================== */

@Composable
private fun CaregiverManageTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        text = "Manage",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Manage the elderly user's information and daily experience.",
        fontSize = 15.sp,
        color = SecondaryText
    )

    Spacer(modifier = Modifier.height(24.dp))


    DashboardButtonRow(
        firstTitle = "Elderly Profile",
        firstAction = {
            onSectionSelected("profile")
        },
        secondTitle = "Family",
        secondAction = {
            onSectionSelected("family")
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    DashboardButtonRow(
        firstTitle = "Memories",
        firstAction = {
            onSectionSelected("memories")
        },
        secondTitle = "Daily Routine",
        secondAction = {
            onSectionSelected("routine")
        }
    )


    Spacer(modifier = Modifier.height(26.dp))


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Personalized Care",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keep the elderly user's profile, family connections, memories and routine personalized.",
                fontSize = 15.sp,
                color = SecondaryText
            )
        }
    }
}


/* ===================================================
   MONITOR TAB
=================================================== */

@Composable
private fun CaregiverMonitorTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        text = "Monitor",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "View activity and progress information.",
        fontSize = 15.sp,
        color = SecondaryText
    )

    Spacer(modifier = Modifier.height(24.dp))


    ProgressCard(
        title = "Game Performance",
        value = "This week's cognitive game activity"
    )

    ProgressCard(
        title = "Activities Completed",
        value = "Overview of completed activities"
    )

    ProgressCard(
        title = "Routine Completion",
        value = "Overview of daily routine"
    )

    ProgressCard(
        title = "Progress Trends",
        value = "Weekly engagement and activity trends"
    )


    Spacer(modifier = Modifier.height(8.dp))


    Button(
        onClick = {
            onSectionSelected("progress")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepTeal
        )
    ) {

        Text(
            text = "Open Progress Details",
            color = Color.White
        )
    }
}


/* ===================================================
   MORE TAB
=================================================== */

@Composable
private fun CaregiverMoreTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        text = "More",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Additional caregiver controls.",
        fontSize = 15.sp,
        color = SecondaryText
    )

    Spacer(modifier = Modifier.height(24.dp))


    DashboardButtonRow(
        firstTitle = "Tasks",
        firstAction = {
            onSectionSelected("tasks")
        },
        secondTitle = "Preferences",
        secondAction = {
            onSectionSelected("preferences")
        }
    )


    Spacer(modifier = Modifier.height(24.dp))


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftMint
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "MIND MITRA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A personalized AI-powered cognitive-care companion for elderly users.",
                fontSize = 15.sp,
                color = SecondaryText
            )
        }
    }
}


/* ===================================================
   TWO BUTTON ROW
=================================================== */

@Composable
private fun DashboardButtonRow(
    firstTitle: String,
    firstAction: () -> Unit,
    secondTitle: String,
    secondAction: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        CaregiverActionButton(
            title = firstTitle,
            modifier = Modifier.weight(1f),
            onClick = firstAction
        )

        CaregiverActionButton(
            title = secondTitle,
            modifier = Modifier.weight(1f),
            onClick = secondAction
        )
    }
}


/* ===================================================
   ACTION BUTTON
=================================================== */

@Composable
private fun CaregiverActionButton(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftMint
        )
    ) {

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTeal
        )
    }
}


/* ===================================================
   SECTION SCREEN
=================================================== */

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
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            Button(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftMint
                )
            ) {

                Text(
                    text = "← Back",
                    color = DeepTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 15.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            content()
        }
    }
}


/* ===================================================
   INFORMATION CARD
=================================================== */

@Composable
private fun SectionInfoCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SecondaryText
            )
        }
    }
}


/* ===================================================
   MEMORY CARD
=================================================== */

@Composable
private fun MemoryCategoryCard(
    title: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            Text(
                text = "Open →",
                fontSize = 14.sp,
                color = DeepTeal,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/* ===================================================
   PREFERENCE CARD
=================================================== */

@Composable
private fun PreferenceCard(
    title: String,
    description: String
) {

    SectionInfoCard(
        title = title,
        description = description
    )
}


/* ===================================================
   TASK CARD
=================================================== */

@Composable
private fun TaskCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Frontend task",
                fontSize = 13.sp,
                color = DeepTeal,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/* ===================================================
   ROUTINE ITEM
=================================================== */

@Composable
private fun RoutineItem(
    time: String,
    activity: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Text(
                text = time,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = activity,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
        }
    }
}


/* ===================================================
   PROGRESS CARD
=================================================== */

@Composable
private fun ProgressCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                color = SecondaryText
            )
        }
    }
}


/* ===================================================
   MONITOR ROW
=================================================== */

@Composable
private fun MonitorRow(
    title: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            fontSize = 15.sp,
            color = SecondaryText
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = DeepTeal,
            fontWeight = FontWeight.SemiBold
        )
    }
}


/* ===================================================
   FRONTEND ACTION BUTTON
=================================================== */

@Composable
private fun FrontendActionButton(
    text: String
) {

    Button(
        onClick = {
            // Backend functionality will be connected later.
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepTeal
        )
    ) {

        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}