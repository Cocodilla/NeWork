package ru.netology.nework.model

import android.net.Uri

data class AttachmentModel(
    val uri: Uri? = null,
    val name: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long = 0L,
    val type: AttachmentType? = null,
)
