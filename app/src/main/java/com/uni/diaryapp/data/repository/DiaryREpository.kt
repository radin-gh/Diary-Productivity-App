package com.uni.diaryapp.data.repository

import com.uni.diaryapp.data.Dao.DiaryDao
import com.uni.diaryapp.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = dao.getAllEntries()

    suspend fun insert(entry: DiaryEntry) {
        dao.insertEntry(entry)
    }

    suspend fun delete(entry: DiaryEntry) {
        dao.deleteEntry(entry)
    }
}











/*suspend fun getDiaryByDate(date: Long): DiaryEntry? {
        return dao.getDiaryByDate(date)
    }*/


/*suspend fun updateEmotion(date: Long, emotion: String?) {
    dao.updateEmotion(date, emotion)
}*/