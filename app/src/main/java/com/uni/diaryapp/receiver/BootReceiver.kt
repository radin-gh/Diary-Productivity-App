
package com.uni.diaryapp.receiver



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            // Re-schedule your daily WorkManager here
            DailyTodoScheduler.scheduleDailyTodoWorker(context)
        }
    }
}

