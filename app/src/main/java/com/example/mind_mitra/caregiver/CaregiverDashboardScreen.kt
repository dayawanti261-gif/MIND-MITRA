package com.example.mind_mitra.caregiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.data.FirebaseRepository
import com.example.mind_mitra.reminder.ReminderReceiver


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

            // =====================================================
            // PROFILE
            // =====================================================

            "profile" -> {

                var patientName by remember {
                    mutableStateOf("Loading...")
                }

                var patientEmail by remember {
                    mutableStateOf("")
                }

                var patientLanguage by remember {
                    mutableStateOf("")
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()

                LaunchedEffect(Unit) {

                    if (caregiverId == null) {
                        message =
                            "Caregiver session not found."

                        return@LaunchedEffect
                    }

                    FirebaseRepository.getConnectedPatientProfile(
                        caregiverId = caregiverId,

                        onSuccess = { profile ->

                            if (profile == null) {

                                patientName =
                                    "No patient connected"

                                message =
                                    "Please connect an elderly user first."

                            } else {

                                patientName =
                                    profile["name"] as? String
                                        ?: "Unknown"

                                patientEmail =
                                    profile["email"] as? String
                                        ?: ""

                                patientLanguage =
                                    profile["language"] as? String
                                        ?: ""
                            }
                        },

                        onError = {
                            message =
                                it.message
                                    ?: "Unable to load profile."
                        }
                    )
                }

                CaregiverSectionScreen(
                    title = "Elderly Profile",
                    subtitle =
                        "View the connected elderly user's information.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    SectionInfoCard(
                        "Name",
                        patientName
                    )

                    SectionInfoCard(
                        "Email",
                        patientEmail.ifEmpty {
                            "Not available"
                        }
                    )

                    SectionInfoCard(
                        "Language",
                        patientLanguage.ifEmpty {
                            "Not available"
                        }
                    )

                    if (message.isNotEmpty()) {

                        Text(
                            text = message,
                            color = SecondaryText
                        )
                    }
                }
            }


            // =====================================================
            // FAMILY
            // =====================================================

            "family" -> {

                var familyList by remember {
                    mutableStateOf<List<Map<String, Any>>>(
                        emptyList()
                    )
                }

                var showAddForm by remember {
                    mutableStateOf(false)
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                fun loadFamily() {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        return
                    }

                    FirebaseRepository.getConnectedPatientFamily(
                        caregiverId = caregiverId,

                        onSuccess = {
                            familyList = it
                        },

                        onError = {
                            message =
                                it.message
                                    ?: "Unable to load family."
                        }
                    )
                }


                LaunchedEffect(Unit) {
                    loadFamily()
                }


                CaregiverSectionScreen(
                    title = "Family",
                    subtitle =
                        "Manage family members connected with the elderly user.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (familyList.isEmpty()) {

                        Text(
                            text =
                                "No family members added yet.",
                            color = SecondaryText
                        )

                    } else {

                        familyList.forEach {

                            SectionInfoCard(
                                title =
                                    it["name"] as? String
                                        ?: "Unknown",

                                description =
                                    it["relation"] as? String
                                        ?: "Family member"
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Button(
                        onClick = {

                            showAddForm =
                                !showAddForm

                            message = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DeepTeal
                            )
                    ) {

                        Text(
                            if (showAddForm)
                                "Cancel"
                            else
                                "Add Family Member"
                        )
                    }


                    if (showAddForm) {

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        AddFamilyForm(

                            onSaved = {

                                showAddForm = false

                                message =
                                    "Family member added successfully."

                                loadFamily()
                            },

                            onError = {
                                message = it
                            }
                        )
                    }


                    if (message.isNotEmpty()) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = message,
                            color = SecondaryText
                        )
                    }
                }
            }


            // =====================================================
            // MEMORIES
            // =====================================================

            "memories" -> {

                var memoryList by remember {
                    mutableStateOf<List<Map<String, Any>>>(
                        emptyList()
                    )
                }

                var showAddForm by remember {
                    mutableStateOf(false)
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                fun loadMemories() {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        return
                    }

                    FirebaseRepository.getConnectedPatientMemories(
                        caregiverId = caregiverId,

                        onSuccess = {
                            memoryList = it
                        },

                        onError = {
                            message =
                                it.message
                                    ?: "Unable to load memories."
                        }
                    )
                }


                LaunchedEffect(Unit) {
                    loadMemories()
                }


                CaregiverSectionScreen(
                    title = "Memory Vault",
                    subtitle =
                        "Manage meaningful memories for the elderly user.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (memoryList.isEmpty()) {

                        Text(
                            text =
                                "No memories added yet.",
                            color = SecondaryText
                        )

                    } else {

                        memoryList.forEach {

                            SectionInfoCard(

                                title =
                                    it["title"] as? String
                                        ?: "Untitled Memory",

                                description =
                                    it["description"] as? String
                                        ?: "No description"
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Button(
                        onClick = {

                            showAddForm =
                                !showAddForm

                            message = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DeepTeal
                            )
                    ) {

                        Text(
                            if (showAddForm)
                                "Cancel"
                            else
                                "Add Memory"
                        )
                    }


                    if (showAddForm) {

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        AddMemoryForm(

                            onSaved = {

                                showAddForm = false

                                message =
                                    "Memory added successfully."

                                loadMemories()
                            },

                            onError = {
                                message = it
                            }
                        )
                    }


                    if (message.isNotEmpty()) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = message,
                            color = SecondaryText
                        )
                    }
                }
            }


            // =====================================================
            // ROUTINE
            // =====================================================

            "routine" -> {

                var routineList by remember {
                    mutableStateOf<List<Map<String, Any>>>(
                        emptyList()
                    )
                }

                var showAddForm by remember {
                    mutableStateOf(false)
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                fun loadRoutine() {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        return
                    }

                    FirebaseRepository.getConnectedPatientSchedule(
                        caregiverId = caregiverId,

                        onSuccess = {
                            routineList = it
                        },

                        onError = {
                            message =
                                it.message
                                    ?: "Unable to load routine."
                        }
                    )
                }


                LaunchedEffect(Unit) {
                    loadRoutine()
                }


                CaregiverSectionScreen(
                    title = "Daily Routine",
                    subtitle =
                        "Manage the user's daily schedule and activities.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (routineList.isEmpty()) {

                        Text(
                            text =
                                "No routine activities added yet.",
                            color = SecondaryText
                        )

                    } else {

                        routineList.forEach {

                            val title =
                                it["title"] as? String
                                    ?: "Activity"

                            val time =
                                it["time"] as? String
                                    ?: "Time not set"

                            val completed =
                                it["completed"] as? Boolean
                                    ?: false


                            RoutineItem(
                                time = time,

                                activity =
                                    "$title ${
                                        if (completed)
                                            "✓"
                                        else
                                            ""
                                    }"
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Button(
                        onClick = {

                            showAddForm =
                                !showAddForm

                            message = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DeepTeal
                            )
                    ) {

                        Text(
                            if (showAddForm)
                                "Cancel"
                            else
                                "Add Routine Activity"
                        )
                    }


                    if (showAddForm) {

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        AddRoutineForm(

                            onSaved = {

                                showAddForm = false

                                message =
                                    "Routine activity added successfully."

                                loadRoutine()
                            },

                            onError = {
                                message = it
                            }
                        )
                    }


                    if (message.isNotEmpty()) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = message,
                            color = SecondaryText
                        )
                    }
                }
            }


            // =====================================================
            // TASKS / REMINDERS
            // =====================================================

            "tasks" -> {

                var reminderList by remember {
                    mutableStateOf<List<Map<String, Any>>>(
                        emptyList()
                    )
                }

                var showAddForm by remember {
                    mutableStateOf(false)
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                fun loadReminders() {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        return
                    }

                    FirebaseRepository.getConnectedPatientReminders(
                        caregiverId = caregiverId,

                        onSuccess = {
                            reminderList = it
                        },

                        onError = {
                            message =
                                it.message
                                    ?: "Unable to load reminders."
                        }
                    )
                }


                LaunchedEffect(Unit) {
                    loadReminders()
                }


                CaregiverSectionScreen(
                    title = "Tasks & Reminders",
                    subtitle =
                        "Manage reminders for the elderly user.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (reminderList.isEmpty()) {

                        Text(
                            text =
                                "No reminders added yet.",
                            color = SecondaryText
                        )

                    } else {

                        reminderList.forEach {

                            val title =
                                it["title"] as? String
                                    ?: "Reminder"

                            val time =
                                it["time"] as? String
                                    ?: "Time not set"

                            val completed =
                                it["completed"] as? Boolean
                                    ?: false


                            TaskCard(
                                title = title,

                                description =
                                    "$time • ${
                                        if (completed)
                                            "Completed"
                                        else
                                            "Pending"
                                    }"
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Button(
                        onClick = {

                            showAddForm =
                                !showAddForm

                            message = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = DeepTeal
                            )
                    ) {

                        Text(
                            if (showAddForm)
                                "Cancel"
                            else
                                "Create New Task"
                        )
                    }


                    if (showAddForm) {

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        AddReminderForm(

                            onSaved = {

                                showAddForm = false

                                message =
                                    "Reminder created successfully."

                                loadReminders()
                            },

                            onError = {
                                message = it
                            }
                        )
                    }


                    if (message.isNotEmpty()) {

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(
                            text = message,
                            color = SecondaryText
                        )
                    }
                }
            }


            // =====================================================
            // PREFERENCES
            // =====================================================

            "preferences" -> {

                var favouriteMusic by remember {
                    mutableStateOf("")
                }

                var favouriteActivities by remember {
                    mutableStateOf("")
                }

                var favouriteMemories by remember {
                    mutableStateOf("")
                }

                var language by remember {
                    mutableStateOf("")
                }

                var isLoading by remember {
                    mutableStateOf(true)
                }

                var isSaving by remember {
                    mutableStateOf(false)
                }

                var message by remember {
                    mutableStateOf("")
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                fun loadPreferences() {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        isLoading = false

                        return
                    }


                    FirebaseRepository.getLinkedPatientId(
                        caregiverId = caregiverId,

                        onSuccess = { patientId ->

                            if (patientId == null) {

                                message =
                                    "No patient connected."

                                isLoading = false

                                return@getLinkedPatientId
                            }


                            FirebaseRepository.getPreferences(
                                userId = patientId,

                                onSuccess = { preferences ->

                                    if (preferences != null) {

                                        favouriteMusic =
                                            preferences[
                                                "favouriteMusic"
                                            ] as? String
                                                ?: ""

                                        favouriteActivities =
                                            preferences[
                                                "favouriteActivities"
                                            ] as? String
                                                ?: ""

                                        favouriteMemories =
                                            preferences[
                                                "favouriteMemories"
                                            ] as? String
                                                ?: ""

                                        language =
                                            preferences[
                                                "language"
                                            ] as? String
                                                ?: ""
                                    }

                                    isLoading = false
                                },

                                onError = {

                                    message =
                                        it.message
                                            ?: "Unable to load preferences."

                                    isLoading = false
                                }
                            )
                        },

                        onError = {

                            message =
                                it.message
                                    ?: "Unable to find connected patient."

                            isLoading = false
                        }
                    )
                }


                LaunchedEffect(Unit) {
                    loadPreferences()
                }


                CaregiverSectionScreen(
                    title = "Preferences",
                    subtitle =
                        "Manage activities and content the elderly user enjoys.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (isLoading) {

                        Text(
                            text =
                                "Loading preferences...",
                            color = SecondaryText
                        )

                    } else {

                        OutlinedTextField(
                            value = favouriteMusic,

                            onValueChange = {
                                favouriteMusic = it
                                message = ""
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            label = {
                                Text("Favourite Music")
                            }
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        OutlinedTextField(
                            value =
                                favouriteActivities,

                            onValueChange = {
                                favouriteActivities = it
                                message = ""
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            label = {
                                Text("Favourite Activities")
                            }
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        OutlinedTextField(
                            value =
                                favouriteMemories,

                            onValueChange = {
                                favouriteMemories = it
                                message = ""
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            label = {
                                Text("Favourite Memories")
                            }
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        OutlinedTextField(
                            value = language,

                            onValueChange = {
                                language = it
                                message = ""
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            label = {
                                Text("Language")
                            },

                            singleLine = true
                        )


                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )


                        Button(
                            onClick = {

                                if (caregiverId == null) {

                                    message =
                                        "Caregiver session not found."

                                    return@Button
                                }

                                isSaving = true
                                message = ""


                                FirebaseRepository.getLinkedPatientId(
                                    caregiverId = caregiverId,

                                    onSuccess = { patientId ->

                                        if (patientId == null) {

                                            isSaving = false

                                            message =
                                                "No patient connected."

                                            return@getLinkedPatientId
                                        }


                                        FirebaseRepository.savePreferences(

                                            userId = patientId,

                                            favouriteMusic =
                                                favouriteMusic.trim(),

                                            favouriteActivities =
                                                favouriteActivities.trim(),

                                            favouriteMemories =
                                                favouriteMemories.trim(),

                                            language =
                                                language.trim(),

                                            onSuccess = {

                                                isSaving = false

                                                message =
                                                    "Preferences saved."
                                            },

                                            onError = {

                                                isSaving = false

                                                message =
                                                    it.message
                                                        ?: "Failed to save preferences."
                                            }
                                        )
                                    },

                                    onError = {

                                        isSaving = false

                                        message =
                                            it.message
                                                ?: "Unable to find connected patient."
                                    }
                                )
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            enabled = !isSaving,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = DeepTeal
                                )
                        ) {

                            Text(
                                if (isSaving)
                                    "Saving..."
                                else
                                    "Save Preferences"
                            )
                        }


                        if (message.isNotEmpty()) {

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text = message,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }


            // =====================================================
            // PROGRESS
            // =====================================================

            "progress" -> {

                var progressList by remember {
                    mutableStateOf<List<Map<String, Any>>>(
                        emptyList()
                    )
                }

                var message by remember {
                    mutableStateOf(
                        "Loading progress..."
                    )
                }

                val caregiverId =
                    AuthRepository.getCurrentUserId()


                LaunchedEffect(Unit) {

                    if (caregiverId == null) {

                        message =
                            "Caregiver session not found."

                        return@LaunchedEffect
                    }


                    FirebaseRepository.getConnectedPatientProgress(
                        caregiverId = caregiverId,

                        onSuccess = {

                            progressList = it

                            message =
                                if (it.isEmpty())
                                    "No game progress available yet."
                                else
                                    ""
                        },

                        onError = {

                            message =
                                it.message
                                    ?: "Unable to load progress."
                        }
                    )
                }


                CaregiverSectionScreen(
                    title = "Progress & Activities",
                    subtitle =
                        "Monitor the elderly user's cognitive game progress.",
                    onBack = {
                        currentSection = "dashboard"
                    }
                ) {

                    if (progressList.isEmpty()) {

                        Text(
                            text = message,
                            color = SecondaryText
                        )

                    } else {

                        progressList.forEach {

                            ProgressCard(

                                title = "Game Progress",

                                value =
                                    "Accuracy: ${
                                        it["accuracy"]
                                            ?: "N/A"
                                    }\n" +
                                            "Level: ${
                                                it["level"]
                                                    ?: "N/A"
                                            }\n" +
                                            "Attempts: ${
                                                it["attempts"]
                                                    ?: "N/A"
                                            }\n" +
                                            "Completion Time: ${
                                                it["completionTime"]
                                                    ?: "N/A"
                                            }"
                            )
                        }
                    }
                }
            }
        }

        return
    }


    // =====================================================
    // MAIN DASHBOARD
    // =====================================================

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(WarmWhite)
    ) {

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(24.dp)
        ) {

            when (currentTab) {

                "home" -> {

                    CaregiverHomeTab(

                        caregiverName =
                            caregiverName,

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


        NavigationBar(
            containerColor = Color.White
        ) {

            NavigationBarItem(

                selected =
                    currentTab == "home",

                onClick = {
                    currentTab = "home"
                },

                icon = {
                    Text(
                        "⌂",
                        fontSize = 22.sp
                    )
                },

                label = {
                    Text("Home")
                }
            )


            NavigationBarItem(

                selected =
                    currentTab == "manage",

                onClick = {
                    currentTab = "manage"
                },

                icon = {
                    Text(
                        "●",
                        fontSize = 20.sp
                    )
                },

                label = {
                    Text("Manage")
                }
            )


            NavigationBarItem(

                selected =
                    currentTab == "monitor",

                onClick = {
                    currentTab = "monitor"
                },

                icon = {
                    Text(
                        "▥",
                        fontSize = 21.sp
                    )
                },

                label = {
                    Text("Monitor")
                }
            )


            NavigationBarItem(

                selected =
                    currentTab == "more",

                onClick = {
                    currentTab = "more"
                },

                icon = {
                    Text(
                        "•••",
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


// =====================================================
// ADD FAMILY FORM
// =====================================================

@Composable
private fun AddFamilyForm(
    onSaved: () -> Unit,
    onError: (String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var relation by remember {
        mutableStateOf("")
    }


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),

        shape =
            RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "Add Family Member",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Name")
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = relation,

                onValueChange = {
                    relation = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Relation")
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Button(

                onClick = {

                    if (name.trim().isEmpty()) {

                        onError(
                            "Please enter a name."
                        )

                        return@Button
                    }


                    if (relation.trim().isEmpty()) {

                        onError(
                            "Please enter the relation."
                        )

                        return@Button
                    }


                    val caregiverId =
                        AuthRepository.getCurrentUserId()


                    if (caregiverId == null) {

                        onError(
                            "Caregiver session not found."
                        )

                        return@Button
                    }


                    FirebaseRepository.getLinkedPatientId(

                        caregiverId = caregiverId,

                        onSuccess = { patientId ->

                            if (patientId == null) {

                                onError(
                                    "No patient connected."
                                )

                            } else {

                                FirebaseRepository.addFamilyMember(

                                    userId = patientId,

                                    name = name.trim(),

                                    relation =
                                        relation.trim(),

                                    onSuccess = {
                                        onSaved()
                                    },

                                    onError = {

                                        onError(
                                            it.message
                                                ?: "Failed to add family member."
                                        )
                                    }
                                )
                            }
                        },

                        onError = {

                            onError(
                                it.message
                                    ?: "Unable to find connected patient."
                            )
                        }
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
            ) {

                Text(
                    "Save Family Member"
                )
            }
        }
    }
}


// =====================================================
// ADD MEMORY FORM
// =====================================================

@Composable
private fun AddMemoryForm(
    onSaved: () -> Unit,
    onError: (String) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var people by remember {
        mutableStateOf("")
    }


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),

        shape =
            RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "Add Memory",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Memory Title")
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = description,

                onValueChange = {
                    description = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Description")
                }
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = people,

                onValueChange = {
                    people = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("People")
                },

                placeholder = {
                    Text(
                        "Example: Priya, Raj"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Button(

                onClick = {

                    if (title.trim().isEmpty()) {

                        onError(
                            "Please enter a memory title."
                        )

                        return@Button
                    }


                    if (description.trim().isEmpty()) {

                        onError(
                            "Please enter a description."
                        )

                        return@Button
                    }


                    val caregiverId =
                        AuthRepository.getCurrentUserId()


                    if (caregiverId == null) {

                        onError(
                            "Caregiver session not found."
                        )

                        return@Button
                    }


                    val peopleList =
                        people.split(",")
                            .map {
                                it.trim()
                            }
                            .filter {
                                it.isNotEmpty()
                            }


                    FirebaseRepository.getLinkedPatientId(

                        caregiverId = caregiverId,

                        onSuccess = { patientId ->

                            if (patientId == null) {

                                onError(
                                    "No patient connected."
                                )

                            } else {

                                FirebaseRepository.addMemory(

                                    userId = patientId,

                                    title =
                                        title.trim(),

                                    description =
                                        description.trim(),

                                    people =
                                        peopleList,

                                    onSuccess = {
                                        onSaved()
                                    },

                                    onError = {

                                        onError(
                                            it.message
                                                ?: "Failed to add memory."
                                        )
                                    }
                                )
                            }
                        },

                        onError = {

                            onError(
                                it.message
                                    ?: "Unable to find connected patient."
                            )
                        }
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
            ) {

                Text(
                    "Save Memory"
                )
            }
        }
    }
}


// =====================================================
// ADD ROUTINE FORM
// =====================================================

@Composable
private fun AddRoutineForm(
    onSaved: () -> Unit,
    onError: (String) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var time by remember {
        mutableStateOf("")
    }


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),

        shape =
            RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "Add Routine Activity",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Activity")
                },

                placeholder = {
                    Text(
                        "Example: Morning Walk"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = time,

                onValueChange = {
                    time = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Time")
                },

                placeholder = {
                    Text(
                        "Example: 08:00 AM"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Button(

                onClick = {

                    if (title.trim().isEmpty()) {

                        onError(
                            "Please enter an activity."
                        )

                        return@Button
                    }


                    if (time.trim().isEmpty()) {

                        onError(
                            "Please enter a time."
                        )

                        return@Button
                    }


                    val caregiverId =
                        AuthRepository.getCurrentUserId()


                    if (caregiverId == null) {

                        onError(
                            "Caregiver session not found."
                        )

                        return@Button
                    }


                    FirebaseRepository.getLinkedPatientId(

                        caregiverId = caregiverId,

                        onSuccess = { patientId ->

                            if (patientId == null) {

                                onError(
                                    "No patient connected."
                                )

                            } else {

                                FirebaseRepository.addSchedule(

                                    userId = patientId,

                                    title =
                                        title.trim(),

                                    time =
                                        time.trim(),

                                    onSuccess = {
                                        onSaved()
                                    },

                                    onError = {

                                        onError(
                                            it.message
                                                ?: "Failed to add routine."
                                        )
                                    }
                                )
                            }
                        },

                        onError = {

                            onError(
                                it.message
                                    ?: "Unable to find connected patient."
                            )
                        }
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
            ) {

                Text(
                    "Save Routine"
                )
            }
        }
    }
}


// =====================================================
// ADD REMINDER FORM
// =====================================================

@Composable
private fun AddReminderForm(
    onSaved: () -> Unit,
    onError: (String) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var time by remember {
        mutableStateOf("")
    }

    val context =
        LocalContext.current


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),

        shape =
            RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "Create Reminder",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Reminder")
                },

                placeholder = {
                    Text(
                        "Example: Take medicine"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = time,

                onValueChange = {
                    time = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Time")
                },

                placeholder = {
                    Text(
                        "Example: 09:00 AM"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Button(

                onClick = {

                    if (title.trim().isEmpty()) {

                        onError(
                            "Please enter a reminder."
                        )

                        return@Button
                    }


                    if (time.trim().isEmpty()) {

                        onError(
                            "Please enter a time."
                        )

                        return@Button
                    }


                    val caregiverId =
                        AuthRepository.getCurrentUserId()


                    if (caregiverId == null) {

                        onError(
                            "Caregiver session not found."
                        )

                        return@Button
                    }


                    FirebaseRepository.getLinkedPatientId(

                        caregiverId = caregiverId,

                        onSuccess = { patientId ->

                            if (patientId == null) {

                                onError(
                                    "No patient connected."
                                )

                            } else {

                                FirebaseRepository.addReminder(

                                    userId = patientId,

                                    title =
                                        title.trim(),

                                    time =
                                        time.trim(),

                                    onSuccess = {

                                        scheduleReminder(

                                            context = context,

                                            title =
                                                title.trim(),

                                            time =
                                                time.trim()
                                        )

                                        onSaved()
                                    },

                                    onError = {

                                        onError(
                                            it.message
                                                ?: "Failed to create reminder."
                                        )
                                    }
                                )
                            }
                        },

                        onError = {

                            onError(
                                it.message
                                    ?: "Unable to find connected patient."
                            )
                        }
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
            ) {

                Text(
                    "Save Reminder"
                )
            }
        }
    }
}


// =====================================================
// REMINDER SCHEDULER
// =====================================================
private fun scheduleReminder(
    context: Context,
    title: String,
    time: String
) {

    try {

        Log.d(
            "MindMitraReminder",
            "Scheduling reminder: $title at $time"
        )

        val parts =
            time.trim()
                .uppercase()
                .split("\\s+".toRegex())

        if (parts.isEmpty()) {
            Log.e(
                "MindMitraReminder",
                "Empty reminder time"
            )
            return
        }

        val timePart = parts[0]

        val amPm =
            if (parts.size > 1) {
                parts[1]
            } else {
                ""
            }

        val hourMinute =
            timePart.split(":")

        if (hourMinute.size != 2) {
            Log.e(
                "MindMitraReminder",
                "Invalid time format: $time"
            )
            return
        }

        var hour =
            hourMinute[0].toInt()

        val minute =
            hourMinute[1].toInt()

        // Convert AM/PM to 24-hour format
        if (amPm == "PM" && hour != 12) {
            hour += 12
        }

        if (amPm == "AM" && hour == 12) {
            hour = 0
        }

        if (
            hour !in 0..23 ||
            minute !in 0..59
        ) {
            Log.e(
                "MindMitraReminder",
                "Invalid time: $hour:$minute"
            )
            return
        }

        // Create alarm time
        val calendar =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )

                // If today's time has already passed,
                // schedule it for tomorrow
                if (
                    timeInMillis <=
                    System.currentTimeMillis()
                ) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        Log.d(
            "MindMitraReminder",
            "Alarm scheduled for: ${calendar.time}"
        )

        // Send reminder information to Receiver
        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
            ).apply {

                putExtra(
                    "title",
                    title
                )

                // IMPORTANT:
                // Receiver needs these to schedule
                // the reminder again tomorrow.
                putExtra(
                    "hour",
                    hour
                )

                putExtra(
                    "minute",
                    minute
                )
            }

        val requestCode =
            title.hashCode()

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d(
            "MindMitraReminder",
            "Reminder alarm scheduled successfully"
        )

    } catch (e: NumberFormatException) {

        Log.e(
            "MindMitraReminder",
            "Invalid number in time: $time",
            e
        )

    } catch (e: Exception) {

        Log.e(
            "MindMitraReminder",
            "Failed to schedule reminder",
            e
        )
    }
}

// =====================================================
// HOME TAB
// =====================================================

@Composable
private fun CaregiverHomeTab(
    caregiverName: String,
    onSectionSelected: (String) -> Unit,
    onMonitorSelected: () -> Unit
) {

    var homeReminders by remember {
        mutableStateOf<List<Map<String, Any>>>(
            emptyList()
        )
    }


    val caregiverId =
        AuthRepository.getCurrentUserId()


    LaunchedEffect(caregiverId) {

        if (caregiverId != null) {

            FirebaseRepository.getConnectedPatientReminders(

                caregiverId = caregiverId,

                onSuccess = {
                    homeReminders = it
                },

                onError = {
                    homeReminders = emptyList()
                }
            )
        }
    }


    Text(
        "Caregiver Dashboard",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(6.dp)
    )


    Text(
        "Welcome, $caregiverName",
        fontSize = 16.sp,
        color = SecondaryText
    )


    Spacer(
        modifier =
            Modifier.height(22.dp)
    )


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = SoftMint
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                "Elderly User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                "Manage the user's profile, memories, routine and preferences.",
                fontSize = 15.sp,
                color = SecondaryText
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Button(
                onClick = {
                    onSectionSelected("profile")
                },

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DeepTeal
                    )
            ) {

                Text(
                    "View Profile"
                )
            }
        }
    }


    Spacer(
        modifier =
            Modifier.height(26.dp)
    )


    // =====================================================
    // TODAY'S REMINDERS
    // =====================================================

    Text(
        "Today's Reminders",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(14.dp)
    )


    if (homeReminders.isEmpty()) {

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(18.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor = LightCard
                )
        ) {

            Text(
                text =
                    "No reminders added yet.",

                modifier =
                    Modifier.padding(18.dp),

                fontSize = 14.sp,

                color =
                    SecondaryText
            )
        }

    } else {

        homeReminders.forEach { reminder ->

            val title =
                reminder["title"] as? String
                    ?: "Reminder"


            val time =
                reminder["time"] as? String
                    ?: "Time not set"


            val completed =
                reminder["completed"] as? Boolean
                    ?: false


            TaskCard(

                title = title,

                description =
                    "$time • ${
                        if (completed)
                            "Completed"
                        else
                            "Pending"
                    }"
            )
        }


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )


        Button(

            onClick = {
                onSectionSelected("tasks")
            },

            modifier =
                Modifier.fillMaxWidth(),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = DeepTeal
                )
        ) {

            Text(
                "View All Reminders"
            )
        }
    }


    Spacer(
        modifier =
            Modifier.height(26.dp)
    )


    // =====================================================
    // QUICK ACTIONS
    // =====================================================

    Text(
        "Quick Actions",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(14.dp)
    )


    DashboardButtonRow(

        "Elderly Profile",
        {
            onSectionSelected("profile")
        },

        "Family",
        {
            onSectionSelected("family")
        }
    )


    Spacer(
        modifier =
            Modifier.height(12.dp)
    )


    DashboardButtonRow(

        "Memories",
        {
            onSectionSelected("memories")
        },

        "Daily Routine",
        {
            onSectionSelected("routine")
        }
    )


    Spacer(
        modifier =
            Modifier.height(26.dp)
    )


    // =====================================================
    // WEEKLY OVERVIEW
    // =====================================================

    Text(
        "Weekly Overview",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(14.dp)
    )


    ProgressCard(
        "Activity Summary",
        "Games, activities and routine information."
    )


    Button(

        onClick =
            onMonitorSelected,

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
    ) {

        Text(
            "View Progress"
        )
    }
}


// =====================================================
// MANAGE TAB
// =====================================================

@Composable
private fun CaregiverManageTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        "Manage",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(20.dp)
    )


    DashboardButtonRow(

        "Elderly Profile",
        {
            onSectionSelected("profile")
        },

        "Family",
        {
            onSectionSelected("family")
        }
    )


    Spacer(
        modifier =
            Modifier.height(12.dp)
    )


    DashboardButtonRow(

        "Memories",
        {
            onSectionSelected("memories")
        },

        "Daily Routine",
        {
            onSectionSelected("routine")
        }
    )
}


// =====================================================
// MONITOR TAB
// =====================================================

@Composable
private fun CaregiverMonitorTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        "Monitor",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(20.dp)
    )


    ProgressCard(
        "Game Performance",
        "View cognitive game performance."
    )


    ProgressCard(
        "Activities Completed",
        "View completed activities."
    )


    ProgressCard(
        "Routine Completion",
        "View daily routine completion."
    )


    Button(

        onClick = {
            onSectionSelected("progress")
        },

        modifier =
            Modifier.fillMaxWidth(),

        colors =
            ButtonDefaults.buttonColors(
                containerColor = DeepTeal
            )
    ) {

        Text(
            "Open Progress Details"
        )
    }
}


// =====================================================
// MORE TAB
// =====================================================

@Composable
private fun CaregiverMoreTab(
    onSectionSelected: (String) -> Unit
) {

    Text(
        "More",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText
    )


    Spacer(
        modifier =
            Modifier.height(20.dp)
    )


    DashboardButtonRow(

        "Tasks",
        {
            onSectionSelected("tasks")
        },

        "Preferences",
        {
            onSectionSelected("preferences")
        }
    )
}


// =====================================================
// TWO BUTTON ROW
// =====================================================

@Composable
private fun DashboardButtonRow(
    firstTitle: String,
    firstAction: () -> Unit,
    secondTitle: String,
    secondAction: () -> Unit
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Button(

            onClick =
                firstAction,

            modifier =
                Modifier
                    .weight(1f)
                    .height(80.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SoftMint
                ),

            shape =
                RoundedCornerShape(18.dp)
        ) {

            Text(
                firstTitle,
                color = DeepTeal,
                fontWeight =
                    FontWeight.SemiBold
            )
        }


        Button(

            onClick =
                secondAction,

            modifier =
                Modifier
                    .weight(1f)
                    .height(80.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SoftMint
                ),

            shape =
                RoundedCornerShape(18.dp)
        ) {

            Text(
                secondTitle,
                color = DeepTeal,
                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}


// =====================================================
// SECTION SCREEN
// =====================================================

@Composable
private fun CaregiverSectionScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(WarmWhite)
    ) {

        Column(

            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(24.dp)
        ) {

            Button(

                onClick =
                    onBack,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SoftMint
                    )
            ) {

                Text(
                    "← Back",
                    color = DeepTeal
                )
            }


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            Text(
                title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(
                subtitle,
                fontSize = 15.sp,
                color = SecondaryText
            )


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            content()
        }
    }
}


// =====================================================
// CARDS
// =====================================================

@Composable
private fun SectionInfoCard(
    title: String,
    description: String
) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = LightCard
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(
                description,
                fontSize = 14.sp,
                color = SecondaryText
            )
        }
    }
}


@Composable
private fun PreferenceCard(
    title: String,
    description: String
) {

    SectionInfoCard(
        title,
        description
    )
}


@Composable
private fun TaskCard(
    title: String,
    description: String
) {

    SectionInfoCard(
        title,
        description
    )
}


@Composable
private fun RoutineItem(
    time: String,
    activity: String
) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = LightCard
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
        ) {

            Text(
                time,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )


            Spacer(
                modifier =
                    Modifier.width(20.dp)
            )


            Text(
                activity,
                fontWeight =
                    FontWeight.SemiBold,
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

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = LightCard
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                value,
                fontSize = 14.sp,
                color = SecondaryText
            )
        }
    }
}