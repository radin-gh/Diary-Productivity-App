import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uni.diaryapp.ui.todo.ToDoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

@SuppressLint("NewApi")
@Composable
fun ToDoScreen(
    viewModel: ToDoViewModel = viewModel(),
    isFromCalendar: Boolean = false,
    selectedDate: LocalDate? = null
) {
    var toDoText by remember { mutableStateOf("") }

    // Decide which tasks to show
    val taskList by if (isFromCalendar) {
        val date = selectedDate ?: LocalDate.now()
        viewModel.getTodosForDate(date).collectAsState(initial = emptyList())

    } else {
        viewModel.todayTodos.collectAsState()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = toDoText,
            onValueChange = { toDoText = it },
            label = { Text("Add a to-do task") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (toDoText.isNotBlank()) {
                val dateToSave = selectedDate ?: LocalDate.now()
                viewModel.addTask(
                    title = toDoText,
                    dueTime = dateToSave.atStartOfDay() // store correct day
                )
                toDoText = ""
            }
        }) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            if (isFromCalendar) "Tasks for ${selectedDate ?: LocalDate.now()}"
            else "Today's Tasks",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn {
            items(taskList) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.title)
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { viewModel.toggleTask(item) }
                    )
                }
            }
        }
    }
}



