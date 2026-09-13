package com.example.mind_mitra.games

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.R
import com.example.mind_mitra.ui.components.MindCard
import com.example.mind_mitra.ui.components.MindEmptyState
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.theme.MindSecondaryText

@Composable
fun GamesHubScreen(
    onBack: (() -> Unit)? = null,
    onPlayGame: (GameType) -> Unit
) {
    val context = LocalContext.current
    var memoryCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val cached = MemoryGameRepository.getCached(context)
        memoryCount = cached.size
        val loaded = MemoryGameRepository.loadMemories(context)
        memoryCount = loaded.size
    }

    MindScreen(onBack = onBack) {
        MindSectionHeader(
            title = stringResource(R.string.games_title),
            subtitle = stringResource(R.string.games_subtitle)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (memoryCount == 0) {
            MindEmptyState(
                title = stringResource(R.string.no_memories_for_games_title),
                body = stringResource(R.string.no_memories_for_games_body)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        GameType.entries.forEach { game ->
            val difficulty = GameProgressManager.getDifficulty(context, game)
            MindCard(modifier = Modifier.padding(bottom = 14.dp)) {
                Text(
                    text = stringResource(game.titleRes),
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = com.example.mind_mitra.ui.theme.MindDarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(game.descRes),
                    fontSize = 18.sp,
                    color = MindSecondaryText,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.difficulty_label,
                        stringResource(
                            when (difficulty) {
                                GameDifficulty.EASY -> R.string.difficulty_easy
                                GameDifficulty.MEDIUM -> R.string.difficulty_medium
                                GameDifficulty.HARD -> R.string.difficulty_hard
                            }
                        )
                    ),
                    fontSize = 17.sp,
                    color = MindSecondaryText
                )
                Spacer(modifier = Modifier.height(14.dp))
                MindPrimaryButton(
                    text = stringResource(R.string.start_game),
                    onClick = { onPlayGame(game) },
                    enabled = memoryCount > 0
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
