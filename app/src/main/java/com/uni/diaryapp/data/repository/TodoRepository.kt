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

    fun getAllTodos(): Flow<List<TodoItem>> = todoDao.getAllTodos()
    fun getAllSync(): List<TodoItem> = todoDao.getAllSync()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodosByDate(date: LocalDate): Flow<List<TodoItem>> {
        val zone = java.time.ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return todoDao.getTodosBetween(startMillis, endMillis)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmName("getTodosByDateSync")
    suspend fun getTodosByDateSync(date: LocalDate): List<TodoItem> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return todoDao.getTodosBetweenSync(startMillis, endMillis)
    }


    suspend fun insert(todo: TodoItem) = todoDao.insert(todo)
    suspend fun update(todo: TodoItem) = todoDao.update(todo)
    suspend fun delete(todo: TodoItem) = todoDao.delete(todo)
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
