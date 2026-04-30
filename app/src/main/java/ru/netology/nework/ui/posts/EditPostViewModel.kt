package ru.netology.nework.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.AttachmentDto
import ru.netology.nework.data.repository.MediaRepository
import ru.netology.nework.data.repository.PostsRepository
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.Coordinates
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val postsRepository: PostsRepository,
    private val usersRepository: UsersRepository,
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PostEditorState())
    val state: StateFlow<PostEditorState> = _state.asStateFlow()

    private var originalPost: Post? = null
    private var loadedPostId: Long? = null

    init {
        loadAvailableUsers()
    }

    fun changeContent(content: String) {
        _state.update { it.copy(content = content, saveError = null) }
    }

    fun load(postId: Long) {
        if (postId == 0L || loadedPostId == postId) return
        loadedPostId = postId

        viewModelScope.launch {
            val post = postsRepository.getById(postId) ?: return@launch
            originalPost = post
            _state.value = PostEditorState(
                id = post.id,
                content = post.content,
                existingMediaUrl = post.mediaUrl,
                existingMediaType = post.mediaType,
                mentionIds = post.mentionIds,
                coordinates = post.coordinates,
                availableUsers = _state.value.availableUsers,
            )
        }
    }

    fun changeAttachment(attachment: AttachmentModel?) {
        _state.update { it.copy(attachment = attachment, saveError = null) }
    }

    fun changeMentionIds(mentionIds: List<Long>) {
        _state.update { it.copy(mentionIds = mentionIds, saveError = null) }
    }

    fun changeCoordinates(coordinates: Coordinates?) {
        _state.update { it.copy(coordinates = coordinates, saveError = null) }
    }

    fun clearCoordinates() {
        _state.update { it.copy(coordinates = null, saveError = null) }
    }

    fun clearAttachment() {
        _state.update {
            it.copy(
                attachment = null,
                existingMediaUrl = null,
                existingMediaType = PostMediaType.NONE,
                saveError = null,
            )
        }
    }

    fun save(onSaved: () -> Unit) = viewModelScope.launch {
        val currentState = _state.value
        val content = currentState.content.trim()
        if (content.isEmpty() || currentState.saving) return@launch
        if (!appAuth.authState.first().authorized) return@launch

        _state.update { it.copy(saving = true, saveError = null) }

        runCatching {
            val uploadedAttachment = uploadAttachment(currentState.attachment)
            val basePost = originalPost ?: buildNewPost(content)
            postsRepository.save(
                basePost.copy(
                    id = currentState.id,
                    content = content,
                    mentionIds = currentState.mentionIds,
                    mediaUrl = uploadedAttachment?.url ?: currentState.existingMediaUrl,
                    mediaType = uploadedAttachment?.toPostMediaType() ?: currentState.existingMediaType,
                    coordinates = currentState.coordinates,
                )
            )
        }.onSuccess { savedPost ->
            originalPost = savedPost
            _state.update { it.copy(id = savedPost.id, saving = false, saveError = null) }
            onSaved()
        }.onFailure { error ->
            _state.update {
                it.copy(
                    saving = false,
                    saveError = error.message ?: "Не удалось сохранить пост",
                )
            }
        }
    }

    private suspend fun uploadAttachment(attachment: AttachmentModel?): AttachmentDto? {
        if (attachment == null) return null
        val uri = attachment.uri ?: return null
        val type = attachment.type ?: return null
        return mediaRepository.upload(
            uri = uri,
            fileName = attachment.name ?: "upload.bin",
            mimeType = attachment.mimeType,
            type = type,
        )
    }

    private suspend fun buildNewPost(content: String): Post {
        val authState = appAuth.authState.first()
        val user = if (authState.id != 0L) {
            usersRepository.getById(authState.id)
        } else {
            null
        }

        return Post(
            id = 0L,
            authorId = user?.id ?: authState.id,
            author = user?.name ?: "Вы",
            authorAvatar = user?.avatar,
            authorJob = user?.job,
            content = content,
            published = "",
            likedByMe = false,
            likeOwnerIds = emptyList(),
            likes = 0,
            link = null,
            ownedByMe = true,
            mentionIds = _state.value.mentionIds,
            mediaUrl = null,
            mediaType = PostMediaType.NONE,
            coordinates = _state.value.coordinates,
        )
    }

    private fun loadAvailableUsers() {
        viewModelScope.launch {
            runCatching { usersRepository.getAll() }
                .onSuccess { users ->
                    _state.update { it.copy(availableUsers = users) }
                }
        }
    }

    private fun AttachmentDto.toPostMediaType(): PostMediaType = runCatching {
        PostMediaType.valueOf(type.uppercase(Locale.ROOT))
    }.getOrDefault(PostMediaType.NONE)
}
