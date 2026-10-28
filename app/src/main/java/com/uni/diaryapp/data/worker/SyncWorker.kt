package com.uni.diaryapp.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uni.diaryapp.DiaryApplication

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        android.util.Log.d("SyncWorker", "Sync started")

        return try {
            val app = applicationContext as DiaryApplication

            android.util.Log.d("SyncWorker", "Calling syncAll()")

            app.container.syncRepository.syncAll()

            android.util.Log.d("SyncWorker", "Sync completed successfully")
            Result.success()

        } catch (e: Exception) {
            android.util.Log.e(
                "SyncWorker",
                "Sync failed: ${e.message}",
                e
            )
            Result.retry()
        }
    }
}