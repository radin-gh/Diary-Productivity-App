// data/dao/TodoDao.kt
package com.uni.diaryapp.data.Dao

import androidx.room.*
import com.uni.diaryapp.data.model.TodoItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY dueDate ASC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos")
    fun getAllSync(): List<TodoItem>


    @Query("SELECT * FROM todos WHERE dueDate >= :startMillis AND dueDate < :endMillis")
    fun getTodosBetween(startMillis: Long, endMillis: Long): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos WHERE dueDate BETWEEN :startMillis AND :endMillis")
    suspend fun getTodosBetweenSync(startMillis: Long, endMillis: Long): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)
}


