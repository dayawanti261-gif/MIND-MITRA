package com.example.mind_mitra.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mind_mitra.data.ReminderItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {

    private const val PREFS = "mind_mitra_alarm_ids"
    private const val KEY_IDS = "scheduled_ids"

    fun scheduleReminders(context: Context, reminders: List<ReminderItem>) {
        try {
            cancelAll(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                return
            }

            val scheduledIds = mutableSetOf<String>()

            reminders
                .filter { it.enabled && !it.completedToday }
                .forEach { reminder ->
                    val triggerAt = computeTriggerMillis(reminder) ?: return@forEach
                    if (triggerAt <= System.currentTimeMillis()) return@forEach

                    val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
                        putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
                        putExtra(ReminderAlarmReceiver.EXTRA_TITLE, reminder.title)
                        putExtra(ReminderAlarmReceiver.EXTRA_BODY, reminder.description)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        reminder.id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pendingIntent
                        )
                    }
                    scheduledIds.add(reminder.id)
                }

            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_IDS, scheduledIds)
                .apply()
        } catch (_: SecurityException) {
            // Exact-alarm permission unavailable — skip scheduling safely.
        } catch (_: Exception) {
            // Never crash the app while scheduling reminders.
        }
    }

    fun cancelAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(KEY_IDS, emptySet()) ?: emptySet()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        ids.forEach { id ->
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        prefs.edit().remove(KEY_IDS).apply()
    }

    private fun computeTriggerMillis(reminder: ReminderItem): Long? {
        val timeParts = parseTime(reminder.time) ?: return null
        val (hour24, minute) = timeParts

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val target = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }

        if (reminder.date.isNotBlank()) {
            val date = try {
                dateFormat.parse(reminder.date)
            } catch (_: Exception) {
                null
            }
            if (date != null) {
                val dateCal = Calendar.getInstance().apply { time = date }
                target.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                target.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                target.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
            }
        }

        if (target.timeInMillis <= System.currentTimeMillis()) {
            if (reminder.repeat == "daily") {
                target.add(Calendar.DAY_OF_YEAR, 1)
            } else {
                return null
            }
        }
        return target.timeInMillis
    }

    private fun parseTime(timeText: String): Pair<Int, Int>? {
        val trimmed = timeText.trim()
        if (trimmed.isEmpty()) return null

        val isPm = trimmed.contains("pm", ignoreCase = true)
        val isAm = trimmed.contains("am", ignoreCase = true)
        val digits = trimmed.replace(Regex("[^0-9:]"), "")
        val parts = digits.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        var hour24 = hour
        if (isAm || isPm) {
            if (isPm && hour < 12) hour24 += 12
            if (isAm && hour == 12) hour24 = 0
        }
        return hour24 to minute
    }
}
