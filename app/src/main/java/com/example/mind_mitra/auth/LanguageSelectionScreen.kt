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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.R
import com.example.mind_mitra.locale.LocaleHelper
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreenTitle
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindDeepTeal
import com.example.mind_mitra.ui.theme.MindSecondaryText
import com.example.mind_mitra.ui.theme.MindWarmWhite

@Composable
fun LanguageSelectionScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindWarmWhite)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        MindScreenTitle(
            title = stringResource(R.string.choose_language_title),
            subtitle = stringResource(R.string.choose_language_subtitle)
        )
        Spacer(modifier = Modifier.height(28.dp))

        LocaleHelper.supportedLanguages.forEach { option ->
            val isSelected = selected == option.code
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { selected = option.code },
                        role = Role.RadioButton
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selected = option.code },
                        colors = RadioButtonDefaults.colors(selectedColor = MindDeepTeal)
                    )
                    Text(
                        text = option.nativeLabel,
                        fontSize = 22.sp,
                        color = MindDarkText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        MindPrimaryButton(
            text = stringResource(R.string.continue_button),
            onClick = {
                LocaleHelper.saveLanguage(context, selected, recreate = true)
                onContinue()
            }
        )
    }
}
