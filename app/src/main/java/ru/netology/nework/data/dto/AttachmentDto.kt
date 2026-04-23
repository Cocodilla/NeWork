package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.PostMediaType

@Serializable
data class AttachmentDto(
    val url: String,
    val type: String,
)

fun AttachmentDto.toPostMediaType(): PostMediaType = runCatching {
    PostMediaType.valueOf(type.uppercase())
}.getOrDefault(PostMediaType.NONE)
