package com.uni.diaryapp.ui.diary

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.uni.diaryapp.data.DiaryDatabase
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.repository.DiaryRepository
import com.uni.diaryapp.data.worker.SyncScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModel(application: Application) : AndroidViewModel(application) {



    private val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()
    private val repository = DiaryRepository(diaryDao)
    private val auth = FirebaseAuth.getInstance()

    private val currentUserUid = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.distinctUntilChanged()

    val diaryList = currentUserUid
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) {
                flowOf(emptyList<DiaryEntry>())
            } else {
                repository.getEntriesForUser(uid)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

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
            val ownerUid = auth.currentUser?.uid ?: return@launch

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
                pin = pin,
                ownerUid = ownerUid
            )

            repository.insert(newEntry)
            SyncScheduler.syncNow(getApplication<Application>().applicationContext)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch

            if (entry.ownerUid != ownerUid) return@launch

            repository.delete(entry)
        }
    }
}