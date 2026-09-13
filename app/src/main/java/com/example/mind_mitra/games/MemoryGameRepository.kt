package com.example.mind_mitra.games

import android.content.Context
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.network.MemoryData
import com.example.mind_mitra.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class GameMemoryItem(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val photoUrl: String?
)

object MemoryGameRepository {

    private const val PREFS = "mind_mitra_game_prefs"
    private const val CACHE_KEY = "memory_game_cache"

    fun getCached(context: Context): List<GameMemoryItem> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CACHE_KEY, "[]") ?: "[]"
        return parseJson(raw)
    }

    suspend fun loadMemories(context: Context): List<GameMemoryItem> = withContext(Dispatchers.IO) {
        val userId = AuthRepository.getCurrentUserId()
        if (userId == null) return@withContext getCached(context)
        try {
            val response = RetrofitClient.apiService.getUserMemories(userId)
            val items = response.memories.mapNotNull { it.toGameMemoryItem() }
            cache(context, items)
            items
        } catch (_: Exception) {
            getCached(context)
        }
    }

    private suspend fun MemoryData.toGameMemoryItem(): GameMemoryItem? {
        val memoryTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        var url = photo_url
        if (url.isNullOrBlank() && !photo_path.isNullOrBlank()) {
            try {
                url = RetrofitClient.apiService.getPhotoUrl(photo_path).signed_url
            } catch (_: Exception) {
                url = null
            }
        }
        return GameMemoryItem(
            id = id,
            title = memoryTitle,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            category = category?.trim()?.takeIf { it.isNotEmpty() },
            photoUrl = url
        )
    }

    private fun cache(context: Context, items: List<GameMemoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("description", item.description ?: "")
                    .put("category", item.category ?: "")
                    .put("photoUrl", item.photoUrl ?: "")
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CACHE_KEY, array.toString())
            .apply()
    }

    private fun parseJson(raw: String): List<GameMemoryItem> = try {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "").trim()
                if (title.isEmpty()) continue
                add(
                    GameMemoryItem(
                        id = obj.optString("id", ""),
                        title = title,
                        description = obj.optString("description", "").takeIf { it.isNotBlank() },
                        category = obj.optString("category", "").takeIf { it.isNotBlank() },
                        photoUrl = obj.optString("photoUrl", "").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
