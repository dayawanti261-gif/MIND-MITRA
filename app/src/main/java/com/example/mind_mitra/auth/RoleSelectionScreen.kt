package com.example.mind_mitra.auth



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepTeal = Color(0xFF146C68)
private val WarmWhite = Color(0xFFF9FBFA)
private val CardBackground = Color(0xFFE8F5F2)
private val DarkText = Color(0xFF183331)
private val SecondaryText = Color(0xFF61716F)

@Composable
fun RoleSelectionScreen(
    onUserSelected: () -> Unit,
    onCaregiverSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Welcome to MIND MITRA",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose how you will use the platform.",
            fontSize = 16.sp,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(
                onClick = onUserSelected,
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground
                )
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "User",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Access your\npersonal experience",
                        fontSize = 14.sp,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Button(
                onClick = onCaregiverSelected,
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground
                )
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Caregiver",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Manage and monitor\ncare activities",
                        fontSize = 14.sp,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}