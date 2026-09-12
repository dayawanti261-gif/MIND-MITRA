package com.example.mind_mitra.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        Log.d(
            "MindMitraReminder",
            "ReminderReceiver fired"
        )

        val title =
            intent.getStringExtra("title")
                ?: "Reminder"

        val hour =
            intent.getIntExtra(
                "hour",
                -1
            )

        val minute =
            intent.getIntExtra(
                "minute",
                -1
            )

        Log.d(
            "MindMitraReminder",
            "Showing notification for: $title"
        )

        // -------------------------------------------------
        // SHOW NOTIFICATION
        // -------------------------------------------------

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId =
            "mind_mitra_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    channelId,
                    "Mind Mitra Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                )

            notificationManager.createNotificationChannel(
                channel
            )
        }

        val notification =
            androidx.core.app.NotificationCompat
                .Builder(
                    context,
                    channelId
                )
                .setSmallIcon(
                    com.example.mind_mitra.R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Mind Mitra Reminder"
                )
                .setContentText(
                    title
                )
                .setPriority(
                    androidx.core.app.NotificationCompat
                        .PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )

        Log.d(
            "MindMitraReminder",
            "Notification sent successfully"
        )

        // -------------------------------------------------
        // SCHEDULE NEXT DAY
        // -------------------------------------------------

        if (
            hour in 0..23 &&
            minute in 0..59
        ) {

            scheduleNextDayReminder(
                context = context,
                title = title,
                hour = hour,
                minute = minute
            )

        } else {

            Log.e(
                "MindMitraReminder",
                "Invalid reminder time. Cannot schedule next day."
            )
        }
    }

    private fun scheduleNextDayReminder(
        context: Context,
        title: String,
        hour: Int,
        minute: Int
    ) {

        val calendar =
            Calendar.getInstance().apply {

                add(
                    Calendar.DAY_OF_YEAR,
                    1
                )

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

        Log.d(
            "MindMitraReminder",
            "Scheduling next day reminder for: ${calendar.time}"
        )

        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
            ).apply {

                putExtra(
                    "title",
                    title
                )

                putExtra(
                    "hour",
                    hour
                )

                putExtra(
                    "minute",
                    minute
                )
            }

        val requestCode =
            title.hashCode()

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d(
            "MindMitraReminder",
            "Next daily reminder scheduled successfully"
        )
    }
}

