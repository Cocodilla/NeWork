package ru.netology.nework.ui.posts

import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.Coordinates
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User

data class PostEditorState(
    val id: Long = 0L,
    val content: String = "",
    val attachment: AttachmentModel? = null,
    val existingMediaUrl: String? = null,
    val existingMediaType: PostMediaType = PostMediaType.NONE,
    val mentionIds: List<Long> = emptyList(),
    val coordinates: Coordinates? = null,
    val availableUsers: List<User> = emptyList(),
    val saving: Boolean = false,
) {
    val canSave: Boolean
        get() = content.trim().isNotEmpty() && !saving
}
