package com.example.mind_mitra.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mind_mitra.R

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Routine reminder"
        val time = inputData.getString(KEY_TIME) ?: ""
        val channelId = "mind_mitra_routines"

        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Daily routine reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Upcoming: $title")
            .setContentText(if (time.isNotBlank()) "Scheduled for $time" else "Time for your activity")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(title.hashCode(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TIME = "time"
    }
}
