package com.uni.diaryapp.receiver

import android.Manifest
import android.R
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uni.diaryapp.data.DiaryDatabase
import com.uni.diaryapp.data.repository.TodoRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DailyTodoWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {

            val db = DiaryDatabase.getDatabase(context)
            val repository = TodoRepository(db.todoDao())

            // Get today's tasks
            val todosToday = repository.getTodosByDate(LocalDate.now()).firstOrNull() ?: emptyList()

            // Build message
            val message = if (todosToday.isNotEmpty()) {
                "✅ You have ${todosToday.size} task(s) for today!"
            } else {
                "📝 You don’t have any tasks today. Maybe add one for tomorrow?"
            }


            showNotification("To-Do Reminder", message)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, "todo_channel")
            .setSmallIcon(com.uni.diaryapp.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}



