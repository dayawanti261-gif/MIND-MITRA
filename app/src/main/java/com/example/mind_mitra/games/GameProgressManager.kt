package com.example.mind_mitra.games

import android.content.Context
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.network.GameProgressRequest
import com.example.mind_mitra.network.GameResultEntry
import com.example.mind_mitra.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GameProgressManager {

    private const val PREFS = "mind_mitra_game_prefs"

    private val _progressVersion = MutableStateFlow(0)
    val progressVersion: StateFlow<Int> = _progressVersion.asStateFlow()

    fun getDifficulty(context: Context, gameType: GameType): GameDifficulty {
        val level = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("difficulty_${gameType.id}", 1)
        return GameDifficulty.fromLevel(level)
    }

    private fun setDifficulty(context: Context, gameType: GameType, difficulty: GameDifficulty) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt("difficulty_${gameType.id}", difficulty.level)
            .apply()
    }

    fun recordRecentResult(context: Context, result: GameSessionResult) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "recent_${result.gameId}"
        val existing = prefs.getString(key, "[]") ?: "[]"
        val array = try { JSONArray(existing) } catch (_: Exception) { JSONArray() }
        val entry = JSONObject()
            .put("score", result.score)
            .put("accuracy", result.accuracy)
            .put("mistakes", result.mistakes)
            .put("completed", result.completed)
            .put("date", todayString())
            .put("timestamp", System.currentTimeMillis())
        array.put(entry)
        while (array.length() > 20) array.remove(0)
        prefs.edit().putString(key, array.toString()).apply()

        val bestKey = "best_${result.gameId}"
        val previousBest = prefs.getFloat(bestKey, 0f)
        if (result.accuracy > previousBest) {
            prefs.edit().putFloat(bestKey, result.accuracy).apply()
        }
    }

    fun getRecentResults(context: Context, gameId: String): List<GameResultEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("recent_$gameId", "[]") ?: "[]"
        return parseResults(raw)
    }

    fun getAllRecentSessions(context: Context): List<Pair<String, GameResultEntry>> {
        return GameType.entries.flatMap { game ->
            getRecentResults(context, game.id).map { game.id to it }
        }.sortedByDescending { (_, entry) ->
            // GameResultEntry doesn't have timestamp - use order in array (last = newest)
            0
        }
    }

    fun getBestScore(context: Context, gameId: String): Float {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat("best_$gameId", 0f)
    }

    fun getBestScoreOverall(context: Context): Float {
        return GameType.entries.map { getBestScore(context, it.id) }.maxOrNull() ?: 0f
    }

    suspend fun saveSession(context: Context, result: GameSessionResult): List<RewardId> {
        recordRecentResult(context, result)
        if (result.completed) {
            incrementCompleted(context)
        }
        val streak = getStreakDays(context)
        val newRewards = RewardManager.evaluate(context, result, streak)

        val userId = AuthRepository.getCurrentUserId()
        if (userId != null) {
            withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.apiService.saveGameProgress(
                        GameProgressRequest(
                            user_id = userId,
                            game_id = result.gameId,
                            accuracy = result.accuracy,
                            time_taken = result.timeTakenSec,
                            mistakes = result.mistakes,
                            attempts = result.attempts,
                            level = result.difficulty.level,
                            completed = result.completed
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }
        evaluateDifficulty(context, GameType.fromId(result.gameId) ?: return newRewards)
        _progressVersion.value++
        return newRewards
    }

    private suspend fun evaluateDifficulty(context: Context, gameType: GameType) {
        val recent = getRecentResults(context, gameType.id)
        if (recent.size < 2) return
        val current = getDifficulty(context, gameType)
        val next = localDifficultyRecommendation(recent, current)
        setDifficulty(context, gameType, next)
    }

    private fun localDifficultyRecommendation(
        recent: List<GameResultEntry>,
        current: GameDifficulty
    ): GameDifficulty {
        val last = recent.takeLast(3)
        if (last.size < 2) return current
        val avg = last.map { it.accuracy }.average().toFloat()
        return when {
            avg >= 80f -> GameDifficulty.adjust(current, 1)
            avg < 50f -> GameDifficulty.adjust(current, -1)
            else -> current
        }
    }

    fun getStreakDays(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("streak_days", 0)
    }

    private fun bumpStreakIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = todayString()
        val last = prefs.getString("last_play_date", "") ?: ""
        val streak = prefs.getInt("streak_days", 0)
        val newStreak = when {
            last == today -> streak
            last.isEmpty() -> 1
            isYesterday(last, today) -> streak + 1
            else -> 1
        }
        prefs.edit().putString("last_play_date", today).putInt("streak_days", newStreak).apply()
    }

    private fun isYesterday(lastDate: String, today: String): Boolean {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val last = fmt.parse(lastDate) ?: return false
            val cal = Calendar.getInstance()
            cal.time = fmt.parse(today) ?: return false
            cal.add(Calendar.DAY_OF_YEAR, -1)
            fmt.format(cal.time) == fmt.format(last)
        } catch (_: Exception) {
            false
        }
    }

    fun totalCompleted(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("total_completed", 0)
    }

    private fun incrementCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt("total_completed", prefs.getInt("total_completed", 0) + 1).apply()
        bumpStreakIfNeeded(context)
    }

    fun weekActivityCount(context: Context): Int {
        val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        var count = 0
        GameType.entries.forEach { game ->
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("recent_${game.id}", "[]") ?: "[]"
            try {
                val array = JSONArray(raw)
                for (i in 0 until array.length()) {
                    val ts = array.getJSONObject(i).optLong("timestamp", 0L)
                    if (ts >= weekAgo) count++
                }
            } catch (_: Exception) {
            }
        }
        return count
    }

    fun hasPlayedAnyGame(context: Context): Boolean {
        return GameType.entries.any { getRecentResults(context, it.id).isNotEmpty() }
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun parseResults(raw: String): List<GameResultEntry> = try {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    GameResultEntry(
                        score = obj.optDouble("score", 0.0).toFloat(),
                        accuracy = obj.optDouble("accuracy", 0.0).toFloat(),
                        mistakes = obj.optInt("mistakes", 0),
                        completed = obj.optBoolean("completed", false)
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
