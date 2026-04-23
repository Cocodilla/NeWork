package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaUploadResponse(
    val url: String,
)
