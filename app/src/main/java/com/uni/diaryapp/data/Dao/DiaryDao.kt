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
    @Query("""
    SELECT * FROM diary_entries
    WHERE ownerUid = :ownerUid AND isDeleted = 0
    ORDER BY date DESC
""")
    fun getAllEntries(ownerUid: String): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Query("""
    SELECT * FROM diary_entries
    WHERE ownerUid = :ownerUid
""")
    suspend fun getAllSync(ownerUid: String): List<DiaryEntry>

    @Query("""
    SELECT * FROM diary_entries
    WHERE ownerUid = :ownerUid AND syncId = :syncId
    LIMIT 1
""")
    suspend fun getDiaryBySyncId(
        ownerUid: String,
        syncId: String
    ): DiaryEntry?


    @Query("""
    SELECT * FROM diary_entries
    WHERE ownerUid = :ownerUid
      AND date = :date
      AND isDeleted = 0
    LIMIT 1
""")
    suspend fun getDiaryByDate(
        ownerUid: String,
        date: Long
    ): DiaryEntry?



    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)


}