package ru.netology.nework.ui.common

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AttachmentType
import ru.netology.nework.util.AttachmentValidation
import ru.netology.nework.util.readAttachmentModel

@Composable
fun AttachmentPickerSection(
    attachment: AttachmentModel?,
    onAttachmentPicked: (AttachmentModel?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedUri(context, uri, onAttachmentPicked)
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedUri(context, uri, onAttachmentPicked)
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        handlePickedUri(context, uri, onAttachmentPicked)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Вложение",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { imageLauncher.launch("image/*") }) {
                Text("Фото")
            }
            Button(onClick = { audioLauncher.launch("audio/*") }) {
                Text("Аудио")
            }
            Button(onClick = { videoLauncher.launch("video/*") }) {
                Text("Видео")
            }
            if (attachment != null) {
                OutlinedButton(onClick = { onAttachmentPicked(null) }) {
                    Text("Убрать")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        attachment?.let {
            AttachmentPreview(attachment = it)
        }
    }
}

private fun handlePickedUri(
    context: Context,
    uri: Uri?,
    onAttachmentPicked: (AttachmentModel?) -> Unit,
) {
    if (uri == null) return
    val model = context.readAttachmentModel(uri)
    val error = AttachmentValidation.validateSize(model.sizeBytes)
    if (error != null) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        return
    }
    onAttachmentPicked(model)
}

@Composable
private fun AttachmentPreview(attachment: AttachmentModel) {
    when (attachment.type) {
        AttachmentType.IMAGE -> {
            Image(
                painter = rememberAsyncImagePainter(attachment.uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        AttachmentType.AUDIO, AttachmentType.VIDEO, null -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = attachment.name ?: "Файл")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (attachment.type) {
                            AttachmentType.AUDIO -> "Аудио"
                            AttachmentType.VIDEO -> "Видео"
                            AttachmentType.IMAGE -> "Фото"
                            null -> "Неизвестный тип"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatSize(attachment.sizeBytes))
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) String.format("%.2f МБ", mb) else String.format("%.0f КБ", kb)
}
