package com.uni.diaryapp.ui.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.data.model.TodoItem
import com.uni.diaryapp.ui.diary.DiaryViewModel
import com.uni.diaryapp.ui.diary.hashPin

import com.uni.diaryapp.ui.todo.ToDoViewModel
import kotlinx.coroutines.flow.filter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaryViewModel: DiaryViewModel = viewModel(),
    todoViewModel: ToDoViewModel = viewModel(),
    onNavigateToDiary: (DiaryEntry) -> Unit,
    onNavigateToDataView: () -> Unit
) {
    val today = LocalDate.now()

    val diaryEntries by diaryViewModel.diaryList.collectAsState()
    val todayDiary = diaryEntries.firstOrNull { entry ->
        val entryDate = Instant.ofEpochMilli(entry.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        entryDate == today
    }

    val todoList by todoViewModel.todayTodos.collectAsState()
    var showPinDialog by remember { mutableStateOf<DiaryEntry?>(null) }

    // how many tasks done
    val doneTasks = todoList.count { it.isDone }
    val totalTasks = todoList.size

    // streak
    val allTodos by todoViewModel.allTodos.collectAsState()
    val streakDays = remember(allTodos) {
        val today = LocalDate.now()
        var streak = 0
        var date = today

        while (true) {
            val tasksOfDay = allTodos.filter {
                Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate() == date
            }
            if (tasksOfDay.isNotEmpty() && tasksOfDay.all { it.isDone }) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        streak
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        //  Header
        Text(
            "📖 My Diary App",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Diary
        Text("📝 Today's Diary", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (todayDiary != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (todayDiary.isLocked) {
                                showPinDialog = todayDiary
                            } else {
                                onNavigateToDiary(todayDiary)
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(todayDiary.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        todayDiary.content.take(3) + "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("No diary entry for today.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // PIN dialog if locked
        showPinDialog?.let { lockedDiary ->
            PinDialog(
                diaryEntry = lockedDiary,
                onPinVerified = {
                    showPinDialog = null
                    onNavigateToDiary(lockedDiary)
                },
                onDismiss = { showPinDialog = null }
            )
        }

        // Today's Tasks
        Text("✅ Today's Tasks", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (todoList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("No tasks for today.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(todoList) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(task.title, style = MaterialTheme.typography.bodyMedium)
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { todoViewModel.toggleTask(task) }
                            )
                        }
                    }
                }
            }
        }

        // Task progress
        if (totalTasks > 0) {
            Text(
                "Progress: $doneTasks / $totalTasks done",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        //  Streak
        if (streakDays > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Text(
                    "🔥 $streakDays-day streak of completing all tasks!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stored Data
        OutlinedButton(
            onClick = { onNavigateToDataView() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📂 View Stored Data")
        }
    }
}

@Composable
fun PinDialog(
    diaryEntry: DiaryEntry,
    onPinVerified: (DiaryEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var inputPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Enter PIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = inputPin,
                    onValueChange = {
                        inputPin = it
                        error = false
                    },
                    label = { Text("PIN Code") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "PIN Input"
                        )
                    },
                    isError = error
                )
                if (error) {
                    Text(
                        "Incorrect PIN. Try again.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hashedInput = hashPin(inputPin)
                    if (hashedInput == diaryEntry.pin) {
                        onPinVerified(diaryEntry)
                    } else {
                        error = true
                    }
                }
            ) {
                Text("Unlock", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}


