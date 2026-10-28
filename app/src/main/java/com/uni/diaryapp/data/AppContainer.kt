package com.uni.diaryapp.data

import android.content.Context
import com.uni.diaryapp.data.repository.DiaryRepository
import com.uni.diaryapp.data.repository.FirestoreRepository
import com.uni.diaryapp.data.repository.SyncRepository
import com.uni.diaryapp.data.repository.TodoRepository

class AppContainer(context: Context) {

    private val database =
        DiaryDatabase.getDatabase(context)

    val diaryRepository =
        DiaryRepository(database.diaryDao())

    val todoRepository =
        TodoRepository(database.todoDao())

    val firestoreRepository =
        FirestoreRepository()

    val syncRepository =
        SyncRepository(
            diaryRepository = diaryRepository,
            todoRepository = todoRepository,
            firestoreRepository = firestoreRepository
        )
}