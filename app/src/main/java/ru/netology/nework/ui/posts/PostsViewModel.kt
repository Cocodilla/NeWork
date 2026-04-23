package ru.netology.nework.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.PostsRepository
import ru.netology.nework.model.Post

data class PostsUiState(
    val loading: Boolean = false,
    val data: List<Post> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PostsUiState())
    val state: StateFlow<PostsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.posts.collectLatest { posts ->
                _state.value = _state.value.copy(
                    loading = false,
                    data = posts,
                )
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                repository.getAll()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Ошибка загрузки постов",
                )
            }
        }
    }

    fun onLike(post: Post) {
        viewModelScope.launch {
            repository.likeById(post.id)
        }
    }

    fun onDelete(postId: Long) {
        viewModelScope.launch {
            repository.removeById(postId)
        }
    }

    fun removeById(postId: Long) {
        onDelete(postId)
    }
}
