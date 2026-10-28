package com.uni.diaryapp.data.repository

// data/repository/TodoRepository.kt


import android.os.Build
import androidx.annotation.RequiresApi
import com.uni.diaryapp.data.Dao.TodoDao
import com.uni.diaryapp.data.model.TodoItem
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(ownerUid: String): Flow<List<TodoItem>> =
        todoDao.getAllTodos(ownerUid)

    suspend fun getAllSync(ownerUid: String): List<TodoItem> =
        todoDao.getAllSync(ownerUid)

    suspend fun getTodoBySyncId(
        ownerUid: String,
        syncId: String
    ): TodoItem? = todoDao.getTodoBySyncId(ownerUid, syncId)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodosByDate(
        ownerUid: String,
        date: LocalDate
    ): Flow<List<TodoItem>> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val endMillis = date.plusDays(1).atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return todoDao.getTodosBetween(ownerUid, startMillis, endMillis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodosByDateSync(
        ownerUid: String,
        date: LocalDate
    ): List<TodoItem> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val endMillis = date.plusDays(1).atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return todoDao.getTodosBetweenSync(ownerUid, startMillis, endMillis)
    }


    suspend fun insert(todo: TodoItem) = todoDao.insert(todo)
    suspend fun update(todo: TodoItem) = todoDao.update(todo)
    suspend fun delete(todo: TodoItem) {
        todoDao.update(
            todo.copy(
                isDeleted = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }


}









/*@RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayTodos(): List<TodoItem> {
        val allTodos = getAllSync() // get all todos from DB
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        return allTodos.filter { todo ->
            val todoDate = Instant.ofEpochMilli(todo.dueDate)
                .atZone(zone)
                .toLocalDate()
            todoDate == today
        }
    }*/
