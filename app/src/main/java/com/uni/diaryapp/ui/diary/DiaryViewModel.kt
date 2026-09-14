package com.uni.diaryapp.ui.diary

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uni.diaryapp.data.DiaryDatabase
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.repository.DiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()
    private val repository = DiaryRepository(diaryDao)


    val diaryList = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    fun addDiaryEntry(
        title: String,
        content: String,
        photos: List<String>? = emptyList(),
        emotion: String? = null,
        selectedDate: LocalDate = LocalDate.now(),
        isLocked: Boolean = false,
        pin: String? = null
    ) {
        viewModelScope.launch {
            val millis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val newEntry = DiaryEntry(
                title = title,
                content = content,
                date = millis,
                emotion = emotion,
                photos = photos,
                isLocked = isLocked,
                pin = pin
            )

            repository.insert(newEntry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }


}
/*class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()

    // Expose diary list as StateFlow
    val diaryList = diaryDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    @RequiresApi(Build.VERSION_CODES.O)
    fun addDiaryEntry(
        title: String,
        content: String,
        photos: List<String>? = emptyList(),
        emotion: String? = null,
        selectedDate: LocalDate = LocalDate.now(),
        isLocked: Boolean = false,
        pin: String? = null
    ) {
        viewModelScope.launch {
            val millis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val newEntry = DiaryEntry(
                title = title,
                content = content,
                date = millis,
                emotion = emotion,
                photos = photos,
                isLocked = isLocked,
                pin = pin
            )
            diaryDao.insertEntry(newEntry)
        }
    }




    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryDao.deleteEntry(entry)
        }
    }
}*/
