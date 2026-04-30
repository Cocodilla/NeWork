package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.PostDto
import ru.netology.nework.data.dto.toRequestDto
import ru.netology.nework.model.Post

@Singleton
class PostsRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    suspend fun getAll(): List<Post> {
        val currentUserId = appAuth.authState.first().id
        val loaded = apiService.getPosts().map { dto: PostDto -> dto.toModel(currentUserId) }
        _posts.value = loaded
        return loaded
    }

    suspend fun getById(id: Long): Post? {
        val current = _posts.value
        return current.firstOrNull { post -> post.id == id }
            ?: getAll().firstOrNull { post -> post.id == id }
    }

    suspend fun save(post: Post): Post {
        val currentUserId = appAuth.authState.first().id
        val saved = apiService.savePost(post.toRequestDto()).toModel(currentUserId)

        val current = _posts.value.toMutableList()
        val index = current.indexOfFirst { it.id == saved.id }
        if (index == -1) {
            current.add(0, saved)
        } else {
            current[index] = saved
        }
        _posts.value = current
        return saved
    }

    suspend fun removeById(id: Long) {
        apiService.removePost(id)
        _posts.value = _posts.value.filterNot { post -> post.id == id }
    }

    suspend fun likeById(id: Long): Post? {
        val current = _posts.value.firstOrNull { post -> post.id == id } ?: return null
        val currentUserId = appAuth.authState.first().id

        val updated = if (current.likedByMe) {
            apiService.dislikePost(id).toModel(currentUserId)
        } else {
            apiService.likePost(id).toModel(currentUserId)
        }

        _posts.value = _posts.value.map { post ->
            if (post.id == id) updated else post
        }
        return updated
    }
}
