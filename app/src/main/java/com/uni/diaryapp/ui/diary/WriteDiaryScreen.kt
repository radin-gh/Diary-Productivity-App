package com.uni.diaryapp.ui.diary


import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import android.Manifest
import android.content.Context
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.input.KeyboardType
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WriteDiaryScreen(
    diaryViewModel: DiaryViewModel,
    selectedDate: LocalDate = LocalDate.now(),
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var images by remember { mutableStateOf<List<File>>(emptyList()) } // فایل واقعی
    var emotion by remember { mutableStateOf("") }

    // Lock + PIN
    var isLocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    val context = LocalContext.current

    // photos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val savedFiles = uris.mapNotNull { saveImageToInternalStorage(context, it) }
        images = images + savedFiles
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Write a New Diary ", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Emoji input
        OutlinedTextField(
            value = emotion,
            onValueChange = { emotion = it.take(2) }, // یک ایموجی
            label = { Text("Emotion (Emoji)") },
            placeholder = { Text("😊") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // photos
        if (images.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images.size) { index ->
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(images[index]),
                            contentDescription = "Selected image",
                            modifier = Modifier.size(100.dp)
                        )
                        IconButton(
                            onClick = { images = images.toMutableList().apply { removeAt(index) } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete image",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lock diary
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isLocked,
                onCheckedChange = { isLocked = it }
            )
            Text("Lock this diary")
        }
        if (isLocked) {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Set PIN (4 digits)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Add Photo")
            }

            Button(onClick = {
                if (title.isNotBlank() && content.text.isNotBlank()) {
                    diaryViewModel.addDiaryEntry(
                        title = title,
                        content = content.text,
                        photos = images.map { it.absolutePath }, // مسیر واقعی فایل
                        selectedDate = selectedDate,
                        emotion = emotion,
                        isLocked = isLocked,
                        pin = if (isLocked) hashPin(pin) else null
                    )
                    onBack()
                }
            }) {
                Text("Save")
            }
        }
    }
}

fun hashPin(pin: String): String {
    val bytes = pin.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) } //change byte to hex
}

fun saveImageToInternalStorage(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

