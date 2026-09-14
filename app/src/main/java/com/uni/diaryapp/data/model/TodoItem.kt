package com.uni.diaryapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val title: String,
    val isDone: Boolean,
    val dueDate: Long
)
