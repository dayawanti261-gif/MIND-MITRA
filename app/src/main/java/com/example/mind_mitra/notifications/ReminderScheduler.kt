package com.example.mind_mitra.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mind_mitra.data.RoutineItem
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleRoutineReminders(context: Context, routines: List<RoutineItem>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_TAG)

        routines.filter { !it.completed }.forEach { routine ->
            val delayMs = delayUntilNextOccurrence(routine.time)
            if (delayMs == null || delayMs < 0) return@forEach

            val input = Data.Builder()
                .putString(ReminderNotificationWorker.KEY_TITLE, routine.title)
                .putString(ReminderNotificationWorker.KEY_TIME, routine.time)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(input)
                .addTag(WORK_TAG)
                .build()

            val workName = "routine_reminder_${routine.id.ifBlank { routine.title }}"
            workManager.enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    private fun delayUntilNextOccurrence(timeText: String): Long? {
        val parts = timeText.trim().split(":", " ")
        if (parts.isEmpty()) return null

        val hour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return null
        var minute = 0
        if (parts.size > 1) {
            minute = parts[1].filter { it.isDigit() }.toIntOrNull() ?: 0
        }
        var isPm = timeText.contains("pm", ignoreCase = true)
        val isAm = timeText.contains("am", ignoreCase = true)
        var hour24 = hour
        if (isAm || isPm) {
            if (isPm && hour < 12) hour24 += 12
            if (isAm && hour == 12) hour24 = 0
        }

        val target = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        if (target.timeInMillis <= System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - System.currentTimeMillis()
    }

    private const val WORK_TAG = "mind_mitra_routine_reminders"
}
