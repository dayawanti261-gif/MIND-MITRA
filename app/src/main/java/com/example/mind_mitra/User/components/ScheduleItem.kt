package com.example.mind_mitra.user.components



import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepTeal = Color(0xFF146C68)
private val DarkText = Color(0xFF183331)

@Composable
fun ScheduleItem(
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
            modifier = Modifier.width(82.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTeal
        )

        Text(
            text = title,
            fontSize = 16.sp,
            color = DarkText
        )

        Spacer(modifier = Modifier.weight(1f))

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