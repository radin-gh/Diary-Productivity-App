package com.uni.diaryapp.data.repository

import kotlinx.coroutines.flow.first

class SyncRepository(
    private val diaryRepository: DiaryRepository,
    private val firestoreRepository: FirestoreRepository,
    private val todoRepository: TodoRepository
) {

    suspend fun syncDiaries() {

        val cloudDiaries = firestoreRepository.getDiaries()

        for (cloudDiary in cloudDiaries) {

            val localDiary =
                diaryRepository.getDiaryBySyncId(cloudDiary.syncId)

            if (cloudDiary.isDeleted) {

                if (localDiary != null) {
                    diaryRepository.update(
                        cloudDiary.copy(
                            id = localDiary.id
                        )
                    )
                }

            } else if (localDiary == null) {

                diaryRepository.insert(cloudDiary)

            } else if (cloudDiary.updatedAt > localDiary.updatedAt) {

                diaryRepository.update(
                    cloudDiary.copy(
                        id = localDiary.id
                    )
                )
            }
        }
    }

    suspend fun uploadLocalDiaries() {

        val localDiaries = diaryRepository.allEntries.first()

        for (localDiary in localDiaries) {
            firestoreRepository.uploadDiary(localDiary)
        }
    }

    suspend fun syncTodos() {

        val cloudTodos = firestoreRepository.getTodos()

        for (cloudTodo in cloudTodos) {

            val localTodo =
                todoRepository.getTodoBySyncId(cloudTodo.syncId)

            if (cloudTodo.isDeleted) {

                if (localTodo != null) {
                    todoRepository.update(
                        cloudTodo.copy(
                            id = localTodo.id
                        )
                    )
                }

            } else if (localTodo == null) {

                todoRepository.insert(cloudTodo)

            } else if (cloudTodo.updatedAt > localTodo.updatedAt) {

                todoRepository.update(
                    cloudTodo.copy(
                        id = localTodo.id
                    )
                )
            }
        }
    }

    suspend fun uploadLocalTodos() {

        val localTodos = todoRepository.getAllTodos().first()

        for (localTodo in localTodos) {
            firestoreRepository.uploadTodo(localTodo)
        }
    }

    suspend fun syncAll() {
        syncDiaries()
        syncTodos()

        uploadLocalDiaries()
        uploadLocalTodos()
    }
}