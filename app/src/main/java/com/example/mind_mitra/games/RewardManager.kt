package com.example.mind_mitra.games

import android.content.Context
import androidx.annotation.StringRes
import com.example.mind_mitra.R

enum class RewardId(val key: String, @StringRes val titleRes: Int, @StringRes val descRes: Int) {
    FIRST_GAME("first_game", R.string.reward_first, R.string.reward_first_desc),
    MEMORY_MASTER("memory_master", R.string.reward_memory_master, R.string.reward_memory_master_desc),
    GREAT_RECALL("great_recall", R.string.reward_great_recall, R.string.reward_great_recall_desc),
    CONSISTENT_MIND("consistent_mind", R.string.reward_consistent_mind, R.string.reward_consistent_mind_desc);

    companion object {
        fun fromKey(key: String): RewardId? = entries.find { it.key == key }
    }
}

object RewardManager {

    private const val PREFS = "mind_mitra_game_prefs"
    private const val EARNED_KEY = "earned_rewards"

    fun getEarnedRewards(context: Context): Set<RewardId> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(EARNED_KEY, emptySet()) ?: emptySet()
        return raw.mapNotNull { RewardId.fromKey(it) }.toSet()
    }

    fun isEarned(context: Context, reward: RewardId): Boolean {
        return getEarnedRewards(context).contains(reward)
    }

    fun evaluate(context: Context, result: GameSessionResult, streakDays: Int): List<RewardId> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val earned = prefs.getStringSet(EARNED_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val newlyEarned = mutableListOf<RewardId>()

        fun award(reward: RewardId) {
            if (earned.add(reward.key)) newlyEarned.add(reward)
        }

        if (result.completed) {
            award(RewardId.FIRST_GAME)
        }

        val memoryGamesCompleted = GameProgressManager.totalCompleted(context)
        if (memoryGamesCompleted >= 5) {
            award(RewardId.MEMORY_MASTER)
        }

        if (result.gameId == GameType.RECALL.id && result.completed && result.accuracy >= 80f) {
            award(RewardId.GREAT_RECALL)
        }

        if (streakDays >= 3) {
            award(RewardId.CONSISTENT_MIND)
        }

        if (newlyEarned.isNotEmpty()) {
            prefs.edit().putStringSet(EARNED_KEY, earned).apply()
        }
        return newlyEarned
    }
}
