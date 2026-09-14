// ui/screens/CalendarScreen.kt
package com.uni.diaryapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uni.diaryapp.ui.diary.DiaryViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    diaryViewModel: DiaryViewModel,
    onAddDiary: (LocalDate) -> Unit,
    onAddTodo: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7

    // ✅ Get all diary entries
    val diaryEntries by diaryViewModel.diaryList.collectAsState()

    // ✅ Map LocalDate -> emotion
    val diaryEmotions = diaryEntries.associate { entry ->
        val date = java.time.Instant.ofEpochMilli(entry.date)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        date to (entry.emotion ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Text("<")
            }
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(320.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Blank spaces for alignment
            items(firstDayOfWeek) { Spacer(modifier = Modifier.size(40.dp)) }

            // Actual days
            items(daysInMonth) { index ->
                val date = month.atDay(index + 1)
                val isSelected = selectedDate == date
                val emoji = diaryEmotions[date] ?: "" // ✅ get emoji for day

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { selectedDate = date }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("${index + 1}")
                            if (emoji.isNotBlank()) {
                                Text(emoji)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons for adding diary/todo
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onAddDiary(selectedDate) }) {
                Text("Add Diary")
            }
            Button(onClick = { onAddTodo(selectedDate) }) {
                Text("Add To-Do")
            }
        }
    }
}

