
package com.uni.diaryapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun checkUser(ownerUid: String) {
        val currentUid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in.")

        require(currentUid == ownerUid) {
            "Cannot access data belonging to another account."
        }
    }

    private fun userDiariesCollection(ownerUid: String) =
        firestore.collection("users")
            .document(ownerUid)
            .collection("diaries")

    private fun userTodosCollection(ownerUid: String) =
        firestore.collection("users")
            .document(ownerUid)
            .collection("todos")

    private fun diaryToMap(entry: DiaryEntry): Map<String, Any?> =
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

    suspend fun uploadDiary(entry: DiaryEntry, ownerUid: String) {
        checkUser(ownerUid)

        require(entry.ownerUid == ownerUid) {
            "Diary owner does not match the signed-in account."
        }

        val document = userDiariesCollection(ownerUid).document(entry.syncId)

        android.util.Log.d(
            "DiarySync",
            "Target path: users/$ownerUid/diaries/${entry.syncId}"
        )

        val snapshot = document.get().await()
        val cloudUpdatedAt = snapshot.getLong("updatedAt") ?: 0L

        android.util.Log.d(
            "DiarySync",
            "Document exists=${snapshot.exists()}, " +
                    "localUpdatedAt=${entry.updatedAt}, " +
                    "cloudUpdatedAt=$cloudUpdatedAt"
        )

        if (!snapshot.exists() || entry.updatedAt > cloudUpdatedAt) {
            checkUser(ownerUid)

            document.set(diaryToMap(entry)).await()

            val verifySnapshot = document.get().await()

            android.util.Log.d(
                "DiarySync",
                "Write verified=${verifySnapshot.exists()}, " +
                        "title=${verifySnapshot.getString("title")}, " +
                        "projectId=${firestore.app.options.projectId}"
            )
        } else {
            android.util.Log.w(
                "DiarySync",
                "Write skipped: cloud document is newer or equally recent."
            )
        }
    }

    suspend fun getDiaries(ownerUid: String): List<DiaryEntry> {
        checkUser(ownerUid)

        val snapshot = userDiariesCollection(ownerUid)
            .get()
            .await()

        checkUser(ownerUid)

        return snapshot.documents.mapNotNull { document ->
            val syncId = document.getString("syncId")
                ?: return@mapNotNull null

            val date = document.getLong("date")
                ?: return@mapNotNull null

            DiaryEntry(
                id = 0,
                syncId = syncId,
                date = date,
                title = document.getString("title") ?: "",
                content = document.getString("content") ?: "",
                photos = document.get("photos") as? List<String>,
                emotion = document.getString("emotion"),
                isLocked = document.getBoolean("isLocked") ?: false,
                pin = document.getString("pin"),
                updatedAt = document.getLong("updatedAt") ?: 0L,
                isDeleted = document.getBoolean("isDeleted") ?: false,
                ownerUid = ownerUid
            )
        }
    }

    private fun todoToMap(todo: TodoItem): Map<String, Any?> =
        mapOf(
            "syncId" to todo.syncId,
            "title" to todo.title,
            "isDone" to todo.isDone,
            "dueDate" to todo.dueDate,
            "updatedAt" to todo.updatedAt,
            "isDeleted" to todo.isDeleted
        )

    suspend fun uploadTodo(todo: TodoItem, ownerUid: String) {
        checkUser(ownerUid)

        require(todo.ownerUid == ownerUid) {
            "Task owner does not match the signed-in account."
        }

        val document = userTodosCollection(ownerUid)
            .document(todo.syncId)

        val snapshot = document.get().await()

        val cloudUpdatedAt = snapshot.getLong("updatedAt") ?: 0L

        if (!snapshot.exists() || todo.updatedAt > cloudUpdatedAt) {
            checkUser(ownerUid)
            document.set(todoToMap(todo)).await()
        }
    }

    suspend fun getTodos(ownerUid: String): List<TodoItem> {
        checkUser(ownerUid)

        val snapshot = userTodosCollection(ownerUid)
            .get()
            .await()

        checkUser(ownerUid)

        return snapshot.documents.mapNotNull { document ->
            val syncId = document.getString("syncId")
                ?: return@mapNotNull null

            val dueDate = document.getLong("dueDate")
                ?: return@mapNotNull null

            TodoItem(
                id = 0,
                syncId = syncId,
                title = document.getString("title") ?: "",
                isDone = document.getBoolean("isDone") ?: false,
                dueDate = dueDate,
                updatedAt = document.getLong("updatedAt") ?: 0L,
                isDeleted = document.getBoolean("isDeleted") ?: false,
                ownerUid = ownerUid
            )
        }
    }
}
