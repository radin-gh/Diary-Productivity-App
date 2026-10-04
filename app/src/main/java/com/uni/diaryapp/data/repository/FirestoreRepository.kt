package com.uni.diaryapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userDiariesCollection() =
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("diaries")

    suspend fun uploadDiary(entry: DiaryEntry) {

        val document = userDiariesCollection()
            .document(entry.syncId)

        val snapshot = document.get().await()

        if (!snapshot.exists()) {
            document.set(
                mapOf(
                    "syncId" to entry.syncId,
                    "date" to entry.date,
                    "title" to entry.title,
                    "content" to entry.content,
                    "photos" to entry.photos,
                    "emotion" to entry.emotion,
                    "isLocked" to entry.isLocked,
                    "pin" to entry.pin,
                    "updatedAt" to entry.updatedAt,
                    "isDeleted" to entry.isDeleted
                )
            ).await()

            return
        }

        val cloudUpdatedAt =
            snapshot.getLong("updatedAt") ?: 0L

        if (entry.updatedAt > cloudUpdatedAt) {
            document.set(
                mapOf(
                    "syncId" to entry.syncId,
                    "date" to entry.date,
                    "title" to entry.title,
                    "content" to entry.content,
                    "photos" to entry.photos,
                    "emotion" to entry.emotion,
                    "isLocked" to entry.isLocked,
                    "pin" to entry.pin,
                    "updatedAt" to entry.updatedAt
                )
            ).await()
        }
    }

    suspend fun getDiaries(): List<DiaryEntry> {

        val snapshot = userDiariesCollection()
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->

            val syncId = document.getString("syncId") ?: return@mapNotNull null
            val date = document.getLong("date") ?: return@mapNotNull null
            val title = document.getString("title") ?: ""
            val content = document.getString("content") ?: ""

            val photos = document.get("photos") as? List<String>
            val emotion = document.getString("emotion")
            val isLocked = document.getBoolean("isLocked") ?: false
            val pin = document.getString("pin")
            val updatedAt = document.getLong("updatedAt") ?: 0L

            DiaryEntry(
                id = 0,
                syncId = syncId,
                date = date,
                title = title,
                content = content,
                photos = photos,
                emotion = emotion,
                isLocked = isLocked,
                pin = pin,
                updatedAt = updatedAt,
                isDeleted = document.getBoolean("isDeleted") ?: false
            )
        }
    }



    private fun userTodosCollection() =
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("todos")


    suspend fun uploadTodo(todo: TodoItem) {

        val document = userTodosCollection()
            .document(todo.syncId)

        val snapshot = document.get().await()

        if (!snapshot.exists()) {
            document.set(
                mapOf(
                    "syncId" to todo.syncId,
                    "title" to todo.title,
                    "isDone" to todo.isDone,
                    "dueDate" to todo.dueDate,
                    "updatedAt" to todo.updatedAt
                )
            ).await()

            return
        }

        val cloudUpdatedAt =
            snapshot.getLong("updatedAt") ?: 0L

        if (todo.updatedAt > cloudUpdatedAt) {
            document.set(
                mapOf(
                    "syncId" to todo.syncId,
                    "title" to todo.title,
                    "isDone" to todo.isDone,
                    "dueDate" to todo.dueDate,
                    "updatedAt" to todo.updatedAt
                )
            ).await()
        }
    }

    suspend fun getTodos(): List<TodoItem> {

        val snapshot = userTodosCollection()
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->

            val syncId = document.getString("syncId")
                ?: return@mapNotNull null

            val title = document.getString("title") ?: ""

            val isDone = document.getBoolean("isDone")
                ?: false

            val dueDate = document.getLong("dueDate")
                ?: return@mapNotNull null

            val updatedAt = document.getLong("updatedAt")
                ?: 0L

            TodoItem(
                id = 0,
                syncId = syncId,
                title = title,
                isDone = isDone,
                dueDate = dueDate,
                updatedAt = updatedAt,
                isDeleted = document.getBoolean("isDeleted") ?: false
            )
        }
    }





}