package com.uni.diaryapp.data.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uni.diaryapp.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)


    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: Long): DiaryEntry?



    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)


    @Query("SELECT * FROM diary_entries WHERE syncId = :syncId LIMIT 1")
    suspend fun getDiaryBySyncId(syncId: String): DiaryEntry?
}