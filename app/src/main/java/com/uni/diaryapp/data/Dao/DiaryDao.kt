package com.uni.diaryapp.data.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uni.diaryapp.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)


    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: Long): DiaryEntry?



    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}