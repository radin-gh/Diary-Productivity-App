package com.uni.diaryapp.ui.todo

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.uni.diaryapp.data.DiaryDatabase
import com.uni.diaryapp.data.model.TodoItem
import com.uni.diaryapp.data.repository.TodoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DiaryDatabase.getDatabase(application)
    private val repository = TodoRepository(db.todoDao())
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

    val allTodos: StateFlow<List<TodoItem>> =
        currentUserUid
            .flatMapLatest { uid ->
                if (uid.isNullOrBlank()) {
                    flowOf(emptyList<TodoItem>())
                } else {
                    repository.getAllTodos(uid)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    @RequiresApi(Build.VERSION_CODES.O)
    val todayTodos: StateFlow<List<TodoItem>> =
        allTodos
            .map { list ->
                val today = LocalDate.now()
                val zone = ZoneId.systemDefault()

                list.filter { todo ->
                    Instant.ofEpochMilli(todo.dueDate)
                        .atZone(zone)
                        .toLocalDate() == today
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodosForDate(date: LocalDate): Flow<List<TodoItem>> {
        return currentUserUid.flatMapLatest { uid ->
            if (uid.isNullOrBlank()) {
                flowOf(emptyList<TodoItem>())
            } else {
                repository.getTodosByDate(uid, date)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTask(
        title: String,
        selectedDate: LocalDate? = null,
        dueTime: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch
            val zone = ZoneId.systemDefault()

            val dateTime = when {
                dueTime != null -> dueTime
                selectedDate != null -> selectedDate.atStartOfDay()
                else -> LocalDate.now().atStartOfDay()
            }

            val millis = dateTime
                .atZone(zone)
                .toInstant()
                .toEpochMilli()

            val newItem = TodoItem(
                id = 0,
                title = title,
                isDone = false,
                dueDate = millis,
                ownerUid = ownerUid
            )

            repository.insert(newItem)
        }
    }

    fun toggleTask(item: TodoItem) {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch

            if (item.ownerUid != ownerUid) return@launch

            repository.update(
                item.copy(
                    isDone = !item.isDone,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTask(item: TodoItem) {
        viewModelScope.launch {
            val ownerUid = auth.currentUser?.uid ?: return@launch

            if (item.ownerUid != ownerUid) return@launch

            repository.delete(item)
        }
    }
}