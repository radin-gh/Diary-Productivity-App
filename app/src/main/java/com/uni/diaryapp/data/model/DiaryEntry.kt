package com.uni.diaryapp.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val content: String,
    val title: String,
    val photos: List<String>? = emptyList(),
    val emotion: String? = null,
    val isLocked: Boolean = false,
    val pin: String? = null,
    val syncId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false

)
