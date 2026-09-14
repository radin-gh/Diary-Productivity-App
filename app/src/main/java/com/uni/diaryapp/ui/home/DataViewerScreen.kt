package com.uni.diaryapp.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem
import com.uni.diaryapp.ui.diary.DiaryViewModel
import com.uni.diaryapp.ui.todo.ToDoViewModel
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
fun diaryEntriesToText(entries: List<DiaryEntry>): String {
    return entries.joinToString(separator = "\n\n") { entry ->
        buildString {
            append("Title: ${entry.title}\n")
            append("Date: ${Instant.ofEpochMilli(entry.date).atZone(ZoneId.systemDefault()).toLocalDate()}\n")
            append("Content: ${entry.content}\n")
            append("Emotion: ${entry.emotion ?: "-"}\n")
            append("Locked: ${entry.isLocked}\n")
            append("Photos: ${entry.photos?.joinToString() ?: "-"}\n")
            append("pin: ${entry.pin}")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun todoItemsToText(items: List<TodoItem>): String {
    return items.joinToString(separator = "\n\n") { item ->
        buildString {
            append("Title: ${item.title}\n")
            append("Due: ${Instant.ofEpochMilli(item.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()}\n")
            append("Done: ${item.isDone}")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DataViewerScreen(
    diaryViewModel: DiaryViewModel = viewModel(),
    todoViewModel: ToDoViewModel = viewModel()
) {
    val diaryEntries by diaryViewModel.diaryList.collectAsState()
    val todoItems by todoViewModel.allTodos.collectAsState()

    val diaryText = diaryEntriesToText(diaryEntries)
    val todoText = todoItemsToText(todoItems)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text("📖 Diary Entries", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(diaryText)

        Spacer(modifier = Modifier.height(24.dp))

        Text("📝 To-Do Items", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(todoText)
    }
}