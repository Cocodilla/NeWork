package ru.netology.nework.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AttachmentType

fun Context.readAttachmentModel(uri: Uri): AttachmentModel {
    val resolver = contentResolver
    val mimeType = resolver.getType(uri)
    val name = resolver.queryDisplayName(uri)
    val size = resolver.querySize(uri)
    val type = when {
        mimeType?.startsWith("image/") == true -> AttachmentType.IMAGE
        mimeType?.startsWith("audio/") == true -> AttachmentType.AUDIO
        mimeType?.startsWith("video/") == true -> AttachmentType.VIDEO
        else -> null
    }

    return AttachmentModel(
        uri = uri,
        name = name,
        mimeType = mimeType,
        sizeBytes = size,
        type = type,
    )
}

private fun ContentResolver.queryDisplayName(uri: Uri): String? =
    querySingleValue(uri, OpenableColumns.DISPLAY_NAME)

private fun ContentResolver.querySize(uri: Uri): Long =
    querySingleValue(uri, OpenableColumns.SIZE)?.toLongOrNull() ?: 0L

private fun ContentResolver.querySingleValue(uri: Uri, column: String): String? {
    val cursor: Cursor = query(uri, null, null, null, null) ?: return null
    cursor.use {
        val index = it.getColumnIndex(column)
        if (index == -1 || !it.moveToFirst()) return null
        return it.getString(index)
    }
}
