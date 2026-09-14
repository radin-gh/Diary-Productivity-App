package com.uni.diaryapp.ui.todo

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.uni.diaryapp.data.DiaryDatabase
import com.uni.diaryapp.data.model.TodoItem
import com.uni.diaryapp.data.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DiaryDatabase.getDatabase(application)

    private val repository = TodoRepository(db.todoDao())

    //  All todos
    val allTodos: StateFlow<List<TodoItem>> =
        repository.getAllTodos()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    //  Today’s todos
    @RequiresApi(Build.VERSION_CODES.O)
    val todayTodos: StateFlow<List<TodoItem>> =
        repository.getAllTodos()
            .map { list ->
                val today = LocalDate.now()
                list.filter { todo ->
                    val todoDate = Instant.ofEpochMilli(todo.dueDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    todoDate == today
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )



    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodosForDate(date: LocalDate): Flow<List<TodoItem>> {
        return repository.getTodosByDate(date)
    }

    // ✅ Add new task
    @RequiresApi(Build.VERSION_CODES.O)
    fun addTask(title: String, selectedDate: LocalDate? = null, dueTime: LocalDateTime? = null) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val millis = when {
                dueTime != null -> {

                    dueTime
                }
                selectedDate != null -> {

                    selectedDate.atStartOfDay()
                }
                else -> {
                    // Default
                    LocalDate.now().atStartOfDay()
                }
            }
                .atZone(zone)
                .toInstant()
                .toEpochMilli()

            val newItem = TodoItem(
                id = 0,
                title = title,
                isDone = false,
                dueDate = millis
            )
            repository.insert(newItem)
        }
    }

    fun toggleTask(item: TodoItem) {
        viewModelScope.launch {
            repository.update(item.copy(isDone = !item.isDone))
        }
    }

    fun deleteTask(item: TodoItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}

