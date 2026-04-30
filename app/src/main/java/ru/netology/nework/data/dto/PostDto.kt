package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Post
import ru.netology.nework.util.toServerUrlOrNull

@Serializable
data class PostDto(
    val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String = "",
    val published: String = "",
    val likedByMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionIds: List<Long> = emptyList(),
    val link: String? = null,
    val attachment: AttachmentDto? = null,
    val coords: CoordinatesDto? = null,
) {
    fun toModel(currentUserId: Long = 0L): Post = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar.toServerUrlOrNull(),
        authorJob = authorJob,
        content = content,
        published = published,
        likedByMe = likedByMe,
        likeOwnerIds = likeOwnerIds,
        likes = likeOwnerIds.size,
        link = link,
        ownedByMe = authorId != 0L && authorId == currentUserId,
        mentionIds = mentionIds,
        mediaUrl = attachment?.url.toServerUrlOrNull(),
        mediaType = attachment?.toPostMediaType() ?: ru.netology.nework.model.PostMediaType.NONE,
        coordinates = coords?.toModel(),
    )
}
