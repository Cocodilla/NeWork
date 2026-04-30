package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType

@Serializable
data class PostRequestDto(
    val id: Long = 0,
    val content: String = "",
    val mentionIds: List<Long> = emptyList(),
    val link: String? = null,
    val attachment: AttachmentDto? = null,
    val coords: CoordinatesDto? = null,
)

fun Post.toRequestDto(): PostRequestDto = PostRequestDto(
    id = id,
    content = content,
    mentionIds = mentionIds,
    link = link?.takeIf { it.isNotBlank() },
    attachment = mediaUrl?.takeIf { mediaType != PostMediaType.NONE }?.let { url ->
        AttachmentDto(
            url = url,
            type = mediaType.name,
        )
    },
    coords = coordinates?.toDto(),
)
