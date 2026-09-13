package com.example.mind_mitra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.R
import com.example.mind_mitra.ui.theme.MindBorder
import com.example.mind_mitra.ui.theme.MindCard
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindDeepTeal
import com.example.mind_mitra.ui.theme.MindErrorBg
import com.example.mind_mitra.ui.theme.MindErrorText
import com.example.mind_mitra.ui.theme.MindSuccessBg
import com.example.mind_mitra.ui.theme.MindSuccessText
import com.example.mind_mitra.ui.theme.MindWarmWhite

val MindTouchHeight = 56.dp
val MindCardRadius = 16.dp
val MindScreenPadding = 20.dp
private val MindPureBlack = Color(0xFF000000)

@Composable
fun MindScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollModifier = if (scrollable) {
        Modifier.verticalScroll(rememberScrollState())
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MindWarmWhite)
            .then(scrollModifier)
            .padding(MindScreenPadding)
    ) {
        if (onBack != null) {
            MindBackButton(onBack = onBack)
            Spacer(modifier = Modifier.height(8.dp))
        }
        content()
    }
}

@Composable
fun MindBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onBack,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MindPureBlack)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_button),
                tint = MindWarmWhite,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.back_button),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MindWarmWhite
            )
        }
    }
}

@Composable
fun MindCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MindCardRadius),
        colors = CardDefaults.cardColors(containerColor = MindCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MindBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

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
        colors = ButtonDefaults.buttonColors(
            containerColor = MindDeepTeal,
            contentColor = MindWarmWhite,
            disabledContainerColor = MindDeepTeal.copy(alpha = 0.5f),
            disabledContentColor = MindWarmWhite
        )
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MindWarmWhite)
    }
}

@Composable
fun MindSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(MindTouchHeight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MindPureBlack)
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MindPureBlack)
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
            fontSize = 18.sp,
            color = MindPureBlack,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MindEmptyState(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MindCardRadius),
        colors = CardDefaults.cardColors(containerColor = MindCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MindBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MindPureBlack)
            Text(
                body,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 18.sp,
                color = MindPureBlack,
                lineHeight = 26.sp
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
        ),
        border = BorderStroke(2.dp, MindPureBlack)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 17.sp,
            color = if (isError) MindErrorText else MindSuccessText,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MindSectionHeader(title: String, subtitle: String? = null) {
    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MindPureBlack)
    if (!subtitle.isNullOrBlank()) {
        Text(
            subtitle,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 18.sp,
            color = MindPureBlack,
            lineHeight = 26.sp
        )
    }
}

@Composable
fun MindLargeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(if (singleLine) 72.dp else 120.dp),
        label = { Text(label, fontSize = 18.sp, color = MindPureBlack, fontWeight = FontWeight.SemiBold) },
        placeholder = { Text(placeholder, fontSize = 18.sp, color = MindPureBlack.copy(alpha = 0.6f)) },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 20.sp,
            color = MindPureBlack,
            fontWeight = FontWeight.Medium
        ),
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MindPureBlack,
            unfocusedBorderColor = MindPureBlack,
            focusedLabelColor = MindPureBlack,
            unfocusedLabelColor = MindPureBlack,
            cursorColor = MindPureBlack,
            focusedTextColor = MindPureBlack,
            unfocusedTextColor = MindPureBlack
        )
    )
}

@Composable
fun MindScreenTitle(title: String, subtitle: String) {
    Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MindPureBlack)
    Text(
        subtitle,
        modifier = Modifier.padding(top = 8.dp),
        fontSize = 18.sp,
        color = MindPureBlack,
        lineHeight = 26.sp
    )
}
