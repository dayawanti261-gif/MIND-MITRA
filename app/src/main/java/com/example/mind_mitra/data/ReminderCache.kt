package com.example.mind_mitra.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ReminderCache {

    private const val PREFS = "mind_mitra_reminder_cache"
    private const val LEGACY_KEY = "reminders_json"

    private fun keyFor(userId: String) = "reminders_$userId"

    fun save(context: Context, userId: String, reminders: List<ReminderItem>) {
        if (userId.isBlank()) return
        val array = JSONArray()
        reminders.forEach { reminder ->
            array.put(
                JSONObject()
                    .put("id", reminder.id)
                    .put("title", reminder.title)
                    .put("description", reminder.description)
                    .put("date", reminder.date)
                    .put("time", reminder.time)
                    .put("repeat", reminder.repeat)
                    .put("enabled", reminder.enabled)
                    .put("lastCompletedDate", reminder.lastCompletedDate ?: "")
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), array.toString())
            .apply()
    }

    fun load(context: Context, userId: String? = null): List<ReminderItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = when {
            !userId.isNullOrBlank() -> prefs.getString(keyFor(userId), null)
                ?: prefs.getString(LEGACY_KEY, null)
            else -> prefs.getString(LEGACY_KEY, null)
        } ?: return emptyList()

        return try {
            parse(raw)
        } catch (_: Exception) {
            if (!userId.isNullOrBlank()) {
                prefs.edit().remove(keyFor(userId)).apply()
            } else {
                prefs.edit().remove(LEGACY_KEY).apply()
            }
            emptyList()
        }
    }

    private fun parse(raw: String): List<ReminderItem> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    ReminderItem(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        date = obj.optString("date", ""),
                        time = obj.optString("time", ""),
                        repeat = obj.optString("repeat", "none"),
                        enabled = obj.optBoolean("enabled", true),
                        lastCompletedDate = obj.optString("lastCompletedDate", "")
                            .takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }
}
