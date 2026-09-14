package com.uni.diaryapp.ui.diary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.uni.diaryapp.data.model.DiaryEntry
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryScreen(
    diaryEntry: DiaryEntry,
    onDeletePhoto: ((String) -> Unit)? = null
) {
    val formattedDate = Instant.ofEpochMilli(diaryEntry.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                diaryEntry.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            if (!diaryEntry.emotion.isNullOrEmpty()) {
                Text(
                    diaryEntry.emotion,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }

            IconButton(onClick = { shareDiary(context, diaryEntry) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Diary"
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                diaryEntry.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // photos
        if (!diaryEntry.photos.isNullOrEmpty()) {
            Text(
                "Photos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(diaryEntry.photos) { path ->
                    val file = File(path)
                    if (file.exists()) {
                        Box(
                            modifier = Modifier.size(200.dp)
                        ) {
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(file),
                                    contentDescription = "Diary Photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            onDeletePhoto?.let { delete ->
                                IconButton(
                                    onClick = { delete(path) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Photo",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun shareDiary(context: Context, diaryEntry: DiaryEntry) {
    val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"

        // text
        val textContent = buildString {
            append("📔 ${diaryEntry.title}\n\n")
            append(diaryEntry.content)
            if (!diaryEntry.emotion.isNullOrEmpty()) {
                append("\n\nMood: ${diaryEntry.emotion}")
            }
        }
        putExtra(Intent.EXTRA_TEXT, textContent)

        // photos
        val uris = ArrayList<Uri>()
        diaryEntry.photos?.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                uris.add(uri)
            }
        }
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(shareIntent, "Share Diary")
    )
}

