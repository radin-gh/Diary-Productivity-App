package com.uni.diaryapp.notifications


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uni.diaryapp.R

private const val CHANNEL_ID = "todo_channel"
private const val CHANNEL_NAME = "To-Do Reminders"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily reminders about your to-do list"
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}


