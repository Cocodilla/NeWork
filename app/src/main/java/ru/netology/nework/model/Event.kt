package ru.netology.nework.model

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val datetime: String,
    val type: EventType = EventType.ONLINE,
    val likedByMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val likes: Int = 0,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val mediaUrl: String? = null,
    val mediaType: PostMediaType = PostMediaType.NONE,
    val coordinates: Coordinates? = null,
)
