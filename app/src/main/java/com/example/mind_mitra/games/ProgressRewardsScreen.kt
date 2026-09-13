package com.example.mind_mitra.games

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.R
import com.example.mind_mitra.ui.components.MindCard
import com.example.mind_mitra.ui.components.MindEmptyState
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindSecondaryText

@Composable
fun ProgressRewardsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val version by GameProgressManager.progressVersion.collectAsState()

    val total = GameProgressManager.totalCompleted(context)
    val streak = GameProgressManager.getStreakDays(context)
    val weekCount = GameProgressManager.weekActivityCount(context)
    val bestScore = GameProgressManager.getBestScoreOverall(context)
    val earnedRewards = RewardManager.getEarnedRewards(context)
    val hasPlayed = GameProgressManager.hasPlayedAnyGame(context)

    MindScreen(onBack = onBack) {
        MindSectionHeader(title = stringResource(R.string.progress_title), subtitle = null)
        Spacer(modifier = Modifier.height(20.dp))

        if (!hasPlayed && total == 0) {
            MindEmptyState(
                title = stringResource(R.string.no_games_completed_title),
                body = stringResource(R.string.no_games_completed_body)
            )
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            StatRow(stringResource(R.string.activities_completed), total.toString())
            StatRow(stringResource(R.string.current_streak), stringResource(R.string.streak_days, streak))
            StatRow(stringResource(R.string.best_score), "${bestScore.toInt()}%")
            StatRow(stringResource(R.string.week_activities), weekCount.toString())
        }

        Spacer(modifier = Modifier.height(20.dp))
        MindSectionHeader(title = stringResource(R.string.recent_activities), subtitle = null)
        Spacer(modifier = Modifier.height(12.dp))

        var anyRecent = false
        GameType.entries.forEach { game ->
            val recent = GameProgressManager.getRecentResults(context, game.id)
            val last = recent.lastOrNull()
            if (last != null) {
                anyRecent = true
                val status = when {
                    last.completed && last.accuracy >= 70f -> stringResource(R.string.status_completed)
                    last.accuracy < 50f -> stringResource(R.string.status_needs_practice)
                    else -> stringResource(R.string.status_stable)
                }
                val diff = GameProgressManager.getDifficulty(context, game)
                val best = GameProgressManager.getBestScore(context, game.id)
                MindCard(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(
                        stringResource(game.titleRes),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MindDarkText
                    )
                    Text(status, fontSize = 18.sp, color = MindSecondaryText, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        stringResource(R.string.best_score_label, best.toInt()),
                        fontSize = 17.sp,
                        color = MindSecondaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        stringResource(
                            R.string.difficulty_label,
                            stringResource(
                                when (diff) {
                                    GameDifficulty.EASY -> R.string.difficulty_easy
                                    GameDifficulty.MEDIUM -> R.string.difficulty_medium
                                    GameDifficulty.HARD -> R.string.difficulty_hard
                                }
                            )
                        ),
                        fontSize = 16.sp,
                        color = MindSecondaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (!anyRecent) {
            Text(
                stringResource(R.string.no_games_completed_body),
                fontSize = 18.sp,
                color = MindSecondaryText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        MindSectionHeader(title = stringResource(R.string.rewards_title), subtitle = null)
        Spacer(modifier = Modifier.height(12.dp))

        if (earnedRewards.isEmpty()) {
            Text(
                stringResource(R.string.no_rewards_yet),
                fontSize = 18.sp,
                color = MindSecondaryText
            )
        } else {
            RewardId.entries.forEach { reward ->
                if (earnedRewards.contains(reward)) {
                    RewardCard(reward.titleRes, reward.descRes)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    MindCard(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(label, fontSize = 18.sp, color = MindSecondaryText)
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MindDarkText, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun RewardCard(titleRes: Int, descRes: Int) {
    MindCard(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(stringResource(titleRes), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MindDarkText)
        Text(stringResource(descRes), fontSize = 17.sp, color = MindSecondaryText, modifier = Modifier.padding(top = 4.dp))
    }
}
