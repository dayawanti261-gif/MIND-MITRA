package com.example.mind_mitra.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mind_mitra.data.ReminderCache

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val cached = ReminderCache.load(context, null)
        if (cached.isNotEmpty()) {
            ReminderScheduler.scheduleReminders(context, cached)
        }
    }
}
