package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Event
import ru.netology.nework.model.PostMediaType

@Serializable
data class EventRequestDto(
    val id: Long = 0,
    val content: String = "",
    val datetime: String = "",
    val type: String = "ONLINE",
    val speakerIds: List<Long> = emptyList(),
    val link: String? = null,
    val attachment: AttachmentDto? = null,
    val coords: CoordinatesDto? = null,
)

fun Event.toRequestDto(): EventRequestDto = EventRequestDto(
    id = id,
    content = content,
    datetime = datetime,
    type = type.name,
    speakerIds = speakerIds,
    link = link?.takeIf { it.isNotBlank() },
    attachment = mediaUrl?.takeIf { mediaType != PostMediaType.NONE }?.let { url ->
        AttachmentDto(
            url = url,
            type = mediaType.name,
        )
    },
    coords = coordinates?.toDto(),
)
