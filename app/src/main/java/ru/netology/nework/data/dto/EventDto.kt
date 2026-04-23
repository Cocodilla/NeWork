package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Event
import ru.netology.nework.model.EventType
import ru.netology.nework.model.PostMediaType

@Serializable
data class EventDto(
    val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String = "",
    val published: String = "",
    val datetime: String = "",
    val type: String = "ONLINE",
    val likedByMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val attachment: AttachmentDto? = null,
    val coords: CoordinatesDto? = null,
) {
    fun toModel(): Event = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        published = published,
        datetime = datetime,
        type = runCatching { EventType.valueOf(type) }.getOrDefault(EventType.ONLINE),
        likedByMe = likedByMe,
        likeOwnerIds = likeOwnerIds,
        likes = likeOwnerIds.size,
        link = link,
        ownedByMe = ownedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        mediaUrl = attachment?.url,
        mediaType = attachment?.toPostMediaType() ?: PostMediaType.NONE,
        coordinates = coords?.toModel(),
    )
}
