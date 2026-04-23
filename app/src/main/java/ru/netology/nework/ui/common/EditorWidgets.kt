package ru.netology.nework.ui.common

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AttachmentType
import ru.netology.nework.model.Coordinates
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User
import ru.netology.nework.ui.map.StaticLocationMap
import ru.netology.nework.util.AttachmentValidation
import ru.netology.nework.util.readAttachmentModel
import ru.netology.nework.util.toDisplayString

private val PreviewSurface = Color(0xFFF6F0FA)
private val AccentColor = Color(0xFF2B1D3F)
private val SubtleColor = Color(0xFF857A92)

@Composable
fun EditorAttachmentPreview(
    attachment: AttachmentModel?,
    existingMediaUrl: String?,
    existingMediaType: PostMediaType,
    onRemove: () -> Unit,
) {
    when {
        attachment?.type == AttachmentType.IMAGE -> {
            PreviewImage(
                model = attachment.uri,
                onRemove = onRemove,
            )
        }

        attachment?.type == AttachmentType.VIDEO -> {
            PreviewFileCard(
                title = attachment.name ?: "Видео готово к публикации",
                subtitle = "Видео будет отправлено вместе с записью",
                onRemove = onRemove,
            )
        }

        attachment?.type == AttachmentType.AUDIO -> {
            PreviewFileCard(
                title = attachment.name ?: "Аудио готово к публикации",
                subtitle = "Аудио будет отправлено вместе с записью",
                onRemove = onRemove,
            )
        }

        !existingMediaUrl.isNullOrBlank() && existingMediaType == PostMediaType.IMAGE -> {
            PreviewImage(
                model = existingMediaUrl,
                onRemove = onRemove,
            )
        }

        !existingMediaUrl.isNullOrBlank() && existingMediaType == PostMediaType.VIDEO -> {
            PreviewFileCard(
                title = "Видео прикреплено",
                subtitle = existingMediaUrl,
                onRemove = onRemove,
            )
        }
    }
}

@Composable
fun UserPickerDialog(
    title: String,
    users: List<User>,
    initiallySelectedIds: Set<Long>,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit,
) {
    var selectedIds by remember(users, initiallySelectedIds) {
        mutableStateOf(initiallySelectedIds)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
            ) {
                items(users, key = { it.id }) { user ->
                    ListItem(
                        modifier = Modifier.clickable {
                            selectedIds = if (selectedIds.contains(user.id)) {
                                selectedIds - user.id
                            } else {
                                selectedIds + user.id
                            }
                        },
                        headlineContent = { Text(user.name) },
                        supportingContent = {
                            val subtitle = listOfNotNull(user.login.takeIf { it.isNotBlank() }, user.job)
                                .joinToString(" • ")
                            if (subtitle.isNotBlank()) {
                                Text(subtitle)
                            }
                        },
                        leadingContent = {
                            Checkbox(
                                checked = selectedIds.contains(user.id),
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + user.id
                                    } else {
                                        selectedIds - user.id
                                    }
                                },
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIds.toList()) }
            ) {
                Text("Готово")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
fun SelectedUsersCard(
    title: String,
    users: List<User>,
) {
    if (users.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = users.joinToString { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = SubtleColor,
            )
        }
    }
}

@Composable
fun LocationPreviewCard(
    title: String,
    coordinates: Coordinates,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PreviewSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AccentColor,
        )
        Text(
            text = coordinates.toDisplayString(),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleColor,
        )
        StaticLocationMap(
            coordinates = coordinates,
            modifier = Modifier.fillMaxWidth(),
        )
        FilledTonalButton(onClick = onRemove) {
            Text("Убрать точку")
        }
    }
}

fun handlePickedAttachment(
    context: Context,
    uri: Uri?,
    allowedTypes: Set<AttachmentType>,
    onAttachmentPicked: (AttachmentModel?) -> Unit,
) {
    if (uri == null) return

    val attachment = context.readAttachmentModel(uri)
    if (attachment.type !in allowedTypes) {
        Toast.makeText(context, "Этот тип вложения здесь не поддерживается", Toast.LENGTH_SHORT).show()
        return
    }

    val sizeError = AttachmentValidation.validateSize(attachment.sizeBytes)
    if (sizeError != null) {
        Toast.makeText(context, sizeError, Toast.LENGTH_SHORT).show()
        return
    }

    onAttachmentPicked(attachment)
}

@Composable
private fun PreviewImage(
    model: Any?,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PreviewSurface),
    ) {
        AsyncImage(
            model = model,
            contentDescription = "Превью вложения",
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentScale = ContentScale.Crop,
        )

        FilledTonalButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) {
            Text("Remove")
        }
    }
}

@Composable
private fun PreviewFileCard(
    title: String,
    subtitle: String,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PreviewSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AccentColor,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleColor,
        )
        FilledTonalButton(onClick = onRemove) {
            Text("Remove")
        }
    }
}
