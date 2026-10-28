// data/dao/TodoDao.kt
package com.uni.diaryapp.data.Dao

import androidx.room.*
import com.uni.diaryapp.data.model.TodoItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface TodoDao {
    @Query("""
    SELECT * FROM todos
    WHERE ownerUid = :ownerUid AND isDeleted = 0
    ORDER BY dueDate ASC
""")
    fun getAllTodos(ownerUid: String): Flow<List<TodoItem>>

    @Query("""
    SELECT * FROM todos
    WHERE ownerUid = :ownerUid
""")
    suspend fun getAllSync(ownerUid: String): List<TodoItem>


    @Query("""
    SELECT * FROM todos
    WHERE ownerUid = :ownerUid
    AND isDeleted = 0
    AND dueDate >= :startMillis
    AND dueDate < :endMillis
""")
    fun getTodosBetween(
        ownerUid: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<TodoItem>>

    @Query("""
    SELECT * FROM todos
    WHERE ownerUid = :ownerUid
    AND isDeleted = 0
    AND dueDate >= :startMillis
    AND dueDate < :endMillis
""")
    suspend fun getTodosBetweenSync(
        ownerUid: String,
        startMillis: Long,
        endMillis: Long
    ): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem)

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)

    @Query("""
    SELECT * FROM todos
    WHERE ownerUid = :ownerUid AND syncId = :syncId
    LIMIT 1
""")
    suspend fun getTodoBySyncId(
        ownerUid: String,
        syncId: String
    ): TodoItem?
}


