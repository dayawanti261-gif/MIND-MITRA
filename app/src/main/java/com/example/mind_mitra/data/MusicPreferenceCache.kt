package com.example.mind_mitra.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object MusicPreferenceCache {

    private const val PREFS = "mind_mitra_music_prefs"
    private const val LEGACY_KEY = "music_preferences"

    private fun keyFor(userId: String) = "music_preferences_$userId"

    fun save(context: Context, userId: String, items: List<MusicPreferenceItem>) {
        if (userId.isBlank()) return
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("label", item.label)
                    .put("searchQuery", item.searchQuery)
                    .put("youtubeUrl", item.youtubeUrl)
                    .put("category", item.category)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), array.toString())
            .apply()
    }

    fun load(context: Context, userId: String): List<MusicPreferenceItem> {
        if (userId.isBlank()) return emptyList()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(keyFor(userId), null)
            ?: prefs.getString(LEGACY_KEY, "[]")
            ?: "[]"

        return try {
            parse(raw)
        } catch (_: Exception) {
            prefs.edit().remove(keyFor(userId)).remove(LEGACY_KEY).apply()
            emptyList()
        }
    }

    private fun parse(raw: String): List<MusicPreferenceItem> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                MusicPreferenceItem.fromMap(
                    mapOf(
                        "id" to obj.optString("id", ""),
                        "label" to obj.optString("label", ""),
                        "searchQuery" to obj.optString("searchQuery", ""),
                        "youtubeUrl" to obj.optString("youtubeUrl", ""),
                        "category" to obj.optString("category", "other")
                    )
                )?.let { add(it) }
            }
        }
    }
}
