package com.example.mind_mitra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.ui.theme.MindCard
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindDeepTeal
import com.example.mind_mitra.ui.theme.MindErrorBg
import com.example.mind_mitra.ui.theme.MindErrorText
import com.example.mind_mitra.ui.theme.MindSecondaryText
import com.example.mind_mitra.ui.theme.MindSoftMint
import com.example.mind_mitra.ui.theme.MindSuccessBg
import com.example.mind_mitra.ui.theme.MindSuccessText

val MindTouchHeight = 56.dp
val MindCardRadius = 20.dp

@Composable
fun MindPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(MindTouchHeight),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MindDeepTeal)
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MindSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(MindTouchHeight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MindDeepTeal)
    }
}

@Composable
fun MindLoadingState(message: String = "Loading…") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MindDeepTeal)
        Text(
            message,
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp,
            color = MindSecondaryText
        )
    }
}

@Composable
fun MindEmptyState(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MindCardRadius),
        colors = CardDefaults.cardColors(containerColor = MindCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MindDarkText)
            Text(
                body,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp,
                color = MindSecondaryText,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun MindStatusBanner(text: String, isError: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) MindErrorBg else MindSuccessBg
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 15.sp,
            color = if (isError) MindErrorText else MindSuccessText,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun MindSectionHeader(title: String, subtitle: String? = null) {
    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MindDarkText)
    if (!subtitle.isNullOrBlank()) {
        Text(
            subtitle,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 15.sp,
            color = MindSecondaryText,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun MindScreenTitle(title: String, subtitle: String) {
    Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MindDarkText)
    Text(
        subtitle,
        modifier = Modifier.padding(top = 8.dp),
        fontSize = 16.sp,
        color = MindSecondaryText,
        lineHeight = 24.sp
    )
}
