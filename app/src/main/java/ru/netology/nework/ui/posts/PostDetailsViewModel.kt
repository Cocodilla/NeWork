package ru.netology.nework.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.PostsRepository
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.model.Post
import ru.netology.nework.model.User

data class PostDetailsUiState(
    val loading: Boolean = false,
    val post: Post? = null,
    val likers: List<User> = emptyList(),
    val mentionedUsers: List<User> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val repository: PostsRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailsUiState())
    val state: StateFlow<PostDetailsUiState> = _state.asStateFlow()

    fun load(postId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
            )

            runCatching {
                val post = repository.getById(postId)
                val users = post?.let { usersRepository.getAll() }.orEmpty()
                val likers = post?.let { currentPost ->
                    users.filter { user -> user.id in currentPost.likeOwnerIds }
                }.orEmpty()
                val mentionedUsers = post?.let { currentPost ->
                    users.filter { user -> user.id in currentPost.mentionIds }
                }.orEmpty()

                PostDetailsUiState(
                    loading = false,
                    post = post,
                    likers = likers,
                    mentionedUsers = mentionedUsers,
                    error = if (post == null) "Пост не найден" else null,
                )
            }.onSuccess { uiState ->
                _state.value = uiState
            }.onFailure { e ->
                _state.value = PostDetailsUiState(
                    loading = false,
                    post = null,
                    likers = emptyList(),
                    mentionedUsers = emptyList(),
                    error = e.message ?: "Ошибка загрузки поста",
                )
            }
        }
    }

    fun onLike() {
        val current = _state.value.post ?: return
        viewModelScope.launch {
            runCatching {
                val updated = repository.likeById(current.id) ?: return@launch
                val users = usersRepository.getAll()
                _state.value = _state.value.copy(
                    post = updated,
                    likers = users.filter { user -> user.id in updated.likeOwnerIds },
                    error = null,
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    error = error.message ?: "Не удалось обновить лайк поста",
                )
            }
        }
    }

    fun onRemove(onRemoved: () -> Unit) {
        val current = _state.value.post ?: return
        viewModelScope.launch {
            runCatching {
                repository.removeById(current.id)
            }.onSuccess {
                onRemoved()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    error = error.message ?: "Не удалось удалить пост",
                )
            }
        }
    }
}
