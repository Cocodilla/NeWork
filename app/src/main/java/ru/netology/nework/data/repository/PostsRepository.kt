package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.AttachmentDto
import ru.netology.nework.data.dto.PostDto
import ru.netology.nework.data.dto.toDto
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User

@Singleton
class PostsRepository @Inject constructor(
    private val apiService: ApiService,
) {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    suspend fun getAll(): List<Post> {
        val demoUsers = fallbackUsers().map { user -> enrichUserDemo(user) }
        val loaded: List<Post> = runCatching {
            apiService.getPosts().map { dto: PostDto -> dto.toModel() }
                .takeIf { it.isNotEmpty() }
                ?: fallbackPosts()
        }.getOrElse {
            fallbackPosts()
        }.mapIndexed { index, post ->
            enrichPostDemo(
                post = post,
                users = demoUsers,
                index = index,
            )
        }
        _posts.value = loaded
        return loaded
    }

    suspend fun getById(id: Long): Post? {
        val current = _posts.value
        return current.firstOrNull { post -> post.id == id }
            ?: getAll().firstOrNull { post -> post.id == id }
    }

    suspend fun save(post: Post): Post {
        val request = post.toDto()
        val saved: Post = runCatching {
            apiService.savePost(request).toModel()
        }.getOrElse {
            if (post.id == 0L) post.copy(id = nextId()) else post
        }

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
        runCatching {
            apiService.removePost(id)
        }
        _posts.value = _posts.value.filterNot { post -> post.id == id }
    }

    suspend fun likeById(id: Long): Post? {
        val current = _posts.value.firstOrNull { post -> post.id == id } ?: return null

        val updated: Post = current.copy(
            likedByMe = !current.likedByMe,
            likes = if (current.likedByMe) {
                (current.likes - 1).coerceAtLeast(0)
            } else {
                current.likes + 1
            },
        )

        _posts.value = _posts.value.map { post ->
            if (post.id == id) updated else post
        }
        return updated
    }

    suspend fun getLikersByPostId(postId: Long): List<User> {
        val post = getById(postId) ?: return emptyList()
        val all = fallbackUsers()

        return when {
            post.likes <= 0 -> emptyList()
            post.likes <= all.size -> all.take(post.likes)
            else -> {
                buildList {
                    repeat(post.likes) { index ->
                        add(
                            all[index % all.size].copy(
                                id = (index + 1).toLong()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun nextId(): Long = (_posts.value.maxOfOrNull { it.id } ?: 0L) + 1L

    private fun fallbackPosts(): List<Post> = listOf(
        Post(
            id = 1,
            authorId = 1,
            author = "Lydia Westervelt",
            authorAvatar = null,
            authorJob = null,
            content = "Слушайте, а как вы относитесь к тому, чтобы собраться большой компанией и поиграть в настолки? У меня есть несколько клёвых игр, можем устроить вечер настолок! Пишите в лс или звоните",
            published = "11.05.2022 11:21",
            likedByMe = false,
            likes = 2,
            link = null,
            ownedByMe = true,
            mentionIds = emptyList(),
            mediaUrl = null,
            mediaType = PostMediaType.NONE,
            coordinates = null,
        ),
        Post(
            id = 2,
            authorId = 2,
            author = "Leo Lipshutz",
            authorAvatar = null,
            authorJob = "Android Developer",
            content = "Шляпа — это головной убор, который носили в Древней Греции. В наше время шляпы носят для защиты от солнца или просто для красоты.",
            published = "21.02.2022 14:23",
            likedByMe = false,
            likes = 10,
            link = null,
            ownedByMe = false,
            mentionIds = emptyList(),
            mediaUrl = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=1200&q=80",
            mediaType = PostMediaType.IMAGE,
            coordinates = null,
        ),
        Post(
            id = 3,
            authorId = 3,
            author = "Kaylynn Mango",
            authorAvatar = null,
            authorJob = null,
            content = "Дайвинг — это увлекательный вид спорта и отдыха, который позволяет погрузиться под воду на глубину до 30 метров и увидеть удивительный подводный мир.",
            published = "19.06.2022 08:59",
            likedByMe = false,
            likes = 10,
            link = null,
            ownedByMe = false,
            mentionIds = emptyList(),
            mediaUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1200&q=80",
            mediaType = PostMediaType.VIDEO,
            coordinates = null,
        ),
    )

    private fun fallbackUsers(): List<User> = listOf(
        User(id = 1, login = "jgummera8", name = "Adison Levin", avatar = "https://i.pravatar.cc/300?u=adison", job = "iOS Developer", about = null),
        User(id = 2, login = "spiunicry", name = "Livia Donin", avatar = "https://i.pravatar.cc/300?u=livia", job = "Android Engineer", about = null),
        User(id = 3, login = "lbragnacpx", name = "Lydia Westervelt", avatar = "https://i.pravatar.cc/300?u=lydia", job = "Product Designer", about = null),
        User(id = 4, login = "smorrill81", name = "Kaylynn Mango", avatar = "https://i.pravatar.cc/300?u=kaylynn", job = "Event Host", about = null),
        User(id = 5, login = "tokenny8q", name = "Leo Lipshutz", avatar = "https://i.pravatar.cc/300?u=leo", job = "QA Lead", about = null),
        User(id = 6, login = "afruen83", name = "Vito Dobbs", avatar = "https://i.pravatar.cc/300?u=vito", job = "Speaker", about = null),
        User(id = 7, login = "thenaughant", name = "Dulce Kenter", avatar = "https://i.pravatar.cc/300?u=dulce", job = "Community Manager", about = null),
        User(id = 8, login = "gprends", name = "Lydia Levin", avatar = "https://i.pravatar.cc/300?u=lydialevin", job = "Motion Designer", about = null),
    )
}

private fun Post.toDto(): PostDto = PostDto(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published,
    likedByMe = likedByMe,
    likeOwnerIds = List(likes) { 0L },
    mentionIds = mentionIds,
    link = link,
    ownedByMe = ownedByMe,
    attachment = mediaUrl?.takeIf { mediaType != PostMediaType.NONE }?.let { url ->
        AttachmentDto(
            url = url,
            type = mediaType.name,
        )
    },
    coords = coordinates?.toDto(),
)
