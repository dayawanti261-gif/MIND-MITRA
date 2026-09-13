package com.example.mind_mitra.games

import androidx.annotation.StringRes
import com.example.mind_mitra.R

enum class GameDifficulty(val level: Int) {
    EASY(1), MEDIUM(2), HARD(3);

    companion object {
        fun fromLevel(level: Int): GameDifficulty = when {
            level >= 3 -> HARD
            level == 2 -> MEDIUM
            else -> EASY
        }

        fun adjust(current: GameDifficulty, direction: Int): GameDifficulty {
            val next = (current.level + direction).coerceIn(1, 3)
            return fromLevel(next)
        }
    }
}

enum class GameType(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int
) {
    MEMORY_MATCH("memory_match", R.string.game_memory_match, R.string.game_memory_match_desc),
    RECALL("recall", R.string.game_recall, R.string.game_recall_desc),
    SEQUENCE("sequence", R.string.game_sequence, R.string.game_sequence_desc);

    companion object {
        fun fromId(id: String): GameType? = entries.find { it.id == id }
    }
}

data class GameSessionResult(
    val gameId: String,
    val score: Float,
    val accuracy: Float,
    val mistakes: Int,
    val attempts: Int,
    val timeTakenSec: Float,
    val completed: Boolean,
    val difficulty: GameDifficulty
)
