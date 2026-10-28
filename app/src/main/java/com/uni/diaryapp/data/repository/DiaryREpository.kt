package com.uni.diaryapp.data.repository

import com.uni.diaryapp.data.Dao.DiaryDao
import com.uni.diaryapp.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {
    fun getEntriesForUser(ownerUid: String): Flow<List<DiaryEntry>> =
        dao.getAllEntries(ownerUid)

    suspend fun insert(entry: DiaryEntry) {
        dao.insertEntry(entry)
    }

    suspend fun delete(entry: DiaryEntry) {
        dao.updateEntry(
            entry.copy(
                isDeleted = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun update(entry: DiaryEntry) {
        dao.updateEntry(entry)
    }

    suspend fun getDiaryByDate(
        ownerUid: String,
        date: Long
    ): DiaryEntry? {
        return dao.getDiaryByDate(ownerUid, date)
    }
    suspend fun getDiaryBySyncId(
        ownerUid: String,
        syncId: String
    ): DiaryEntry? = dao.getDiaryBySyncId(ownerUid, syncId)

    suspend fun getAllSync(ownerUid: String): List<DiaryEntry> =
        dao.getAllSync(ownerUid)

}











/*suspend fun getDiaryByDate(date: Long): DiaryEntry? {
        return dao.getDiaryByDate(date)
    }*/


/*suspend fun updateEmotion(date: Long, emotion: String?) {
    dao.updateEmotion(date, emotion)
}*/