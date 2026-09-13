package com.example.mind_mitra.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mind_mitra.R
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreenTitle
import com.example.mind_mitra.ui.theme.MindWarmWhite

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindWarmWhite)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MindScreenTitle(
            title = stringResource(R.string.welcome_title),
            subtitle = stringResource(R.string.welcome_tagline)
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Text(
            text = stringResource(R.string.welcome_body),
            fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
            color = com.example.mind_mitra.ui.theme.MindSecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        MindPrimaryButton(
            text = stringResource(R.string.welcome_get_started),
            onClick = onGetStarted
        )
    }
}
