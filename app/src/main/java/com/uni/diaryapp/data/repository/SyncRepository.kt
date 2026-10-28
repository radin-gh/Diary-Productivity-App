
package com.uni.diaryapp.data.repository

import com.google.firebase.auth.FirebaseAuth

class SyncRepository(
    private val diaryRepository: DiaryRepository,
    private val firestoreRepository: FirestoreRepository,
    private val todoRepository: TodoRepository
) {
    private val auth = FirebaseAuth.getInstance()

    private fun requireUserUid(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in to sync.")

    suspend fun syncDiaries() {
        syncDiariesForUser(requireUserUid())
    }

    private suspend fun syncDiariesForUser(ownerUid: String) {
        val cloudDiaries = firestoreRepository.getDiaries(ownerUid)

        for (cloudDiary in cloudDiaries) {
            val localDiary = diaryRepository.getDiaryBySyncId(
                ownerUid = ownerUid,
                syncId = cloudDiary.syncId
            )

            if (localDiary == null) {
                diaryRepository.insert(cloudDiary.copy(ownerUid = ownerUid))
            } else if (cloudDiary.updatedAt > localDiary.updatedAt) {
                diaryRepository.update(
                    cloudDiary.copy(
                        id = localDiary.id,
                        ownerUid = ownerUid
                    )
                )
            }
        }
    }

    suspend fun uploadLocalDiaries() {
        uploadLocalDiariesForUser(requireUserUid())
    }

    private suspend fun uploadLocalDiariesForUser(ownerUid: String) {
        val localDiaries = diaryRepository.getAllSync(ownerUid)



        for (diary in localDiaries) {

            android.util.Log.d(
                "DiarySync",
                "Uploading diary: title=${diary.title}, " +
                        "ownerUid=${diary.ownerUid}, syncId=${diary.syncId}"
            )

            if (diary.ownerUid != ownerUid) continue

            firestoreRepository.uploadDiary(
                entry = diary,
                ownerUid = ownerUid
            )

            android.util.Log.d(
                "DiarySync",
                "Upload call completed: ${diary.title}"
            )
        }

    }

    suspend fun syncTodos() {
        syncTodosForUser(requireUserUid())
    }

    private suspend fun syncTodosForUser(ownerUid: String) {
        val cloudTodos = firestoreRepository.getTodos(ownerUid)

        for (cloudTodo in cloudTodos) {
            val localTodo = todoRepository.getTodoBySyncId(
                ownerUid = ownerUid,
                syncId = cloudTodo.syncId
            )

            if (localTodo == null) {
                todoRepository.insert(cloudTodo.copy(ownerUid = ownerUid))
            } else if (cloudTodo.updatedAt > localTodo.updatedAt) {
                todoRepository.update(
                    cloudTodo.copy(
                        id = localTodo.id,
                        ownerUid = ownerUid
                    )
                )
            }
        }
    }

    suspend fun uploadLocalTodos() {
        uploadLocalTodosForUser(requireUserUid())
    }

    private suspend fun uploadLocalTodosForUser(ownerUid: String) {
        val localTodos = todoRepository.getAllSync(ownerUid)

        for (todo in localTodos) {
            if (todo.ownerUid != ownerUid) continue

            firestoreRepository.uploadTodo(
                todo = todo,
                ownerUid = ownerUid
            )
        }
    }

    suspend fun syncAll() {
        // Capture one UID for the entire sync operation.
        val ownerUid = requireUserUid()

        syncDiariesForUser(ownerUid)
        syncTodosForUser(ownerUid)

        uploadLocalDiariesForUser(ownerUid)
        uploadLocalTodosForUser(ownerUid)
    }
}
