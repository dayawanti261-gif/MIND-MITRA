package com.example.mind_mitra.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object RoutineCache {

    private const val PREFS = "mind_mitra_routine_cache"
    private const val LEGACY_KEY = "routines"

    private fun keyFor(userId: String) = "routines_$userId"

    fun save(context: Context, userId: String, routines: List<RoutineItem>) {
        if (userId.isBlank()) return
        val array = JSONArray()
        routines.forEach { routine ->
            array.put(
                JSONObject()
                    .put("id", routine.id)
                    .put("title", routine.title)
                    .put("time", routine.time)
                    .put("daysOfWeek", routine.daysOfWeek)
                    .put("enabled", routine.enabled)
                    .put("reminderNote", routine.reminderNote)
                    .put("lastCompletedDate", routine.lastCompletedDate ?: "")
                    .put("timestamp", routine.timestamp)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), array.toString())
            .apply()
    }

    fun load(context: Context, userId: String): List<RoutineItem> {
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

    private fun parse(raw: String): List<RoutineItem> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    RoutineItem(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        time = obj.optString("time", ""),
                        daysOfWeek = obj.optString("daysOfWeek", "Every day"),
                        enabled = obj.optBoolean("enabled", true),
                        reminderNote = obj.optString("reminderNote", ""),
                        lastCompletedDate = obj.optString("lastCompletedDate", "")
                            .takeIf { it.isNotBlank() },
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
        }
    }
}
