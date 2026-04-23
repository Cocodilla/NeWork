package ru.netology.nework.data.repository

import kotlin.math.absoluteValue
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.User

private data class DemoMedia(
    val url: String,
    val type: PostMediaType,
)

private val demoJobs = listOf(
    "Android Engineer",
    "iOS Developer",
    "Product Designer",
    "Backend Engineer",
    "Community Manager",
    "Motion Designer",
    "QA Lead",
    "DevRel",
)

private val demoAbouts = listOf(
    "Люблю собирать спокойные интерфейсы и живые пользовательские сценарии.",
    "Делаю мобильные экраны аккуратными, быстрыми и понятными.",
    "Работаю на стыке продукта, дизайна и инженерии.",
    "Собираю надёжные API и люблю, когда данные приходят вовремя.",
    "Упаковываю сложные идеи в дружелюбный продуктовый опыт.",
    "Добавляю проектам ритм, движение и визуальный характер.",
)

private val demoImages = listOf(
    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=1200&q=80",
)

private val demoVideos = listOf(
    "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80",
)

internal fun enrichUserDemo(user: User, fallback: User? = null): User {
    val seed = user.login.ifBlank { "${user.id}" }
    return user.copy(
        avatar = user.avatar ?: fallback?.avatar ?: "https://i.pravatar.cc/600?u=$seed",
        job = user.job ?: fallback?.job ?: pickById(user.id, demoJobs),
        about = user.about ?: fallback?.about ?: pickById(user.id, demoAbouts),
    )
}

internal fun enrichPostDemo(
    post: Post,
    users: List<User>,
    index: Int,
): Post {
    val author = users.firstOrNull { it.id == post.authorId } ?: enrichUserDemo(
        User(
            id = post.authorId,
            login = post.author.lowercase().replace(' ', '.'),
            name = post.author,
        )
    )
    val fallbackMedia = when {
        post.mediaType != PostMediaType.NONE && !post.mediaUrl.isNullOrBlank() -> null
        index % 3 == 1 -> pickVideo(post.id + index)
        index % 3 == 2 -> pickImage(post.id + index)
        else -> null
    }

    return post.copy(
        authorAvatar = post.authorAvatar ?: author.avatar,
        authorJob = post.authorJob ?: author.job,
        mediaUrl = post.mediaUrl ?: fallbackMedia?.url,
        mediaType = if (post.mediaType != PostMediaType.NONE) {
            post.mediaType
        } else {
            fallbackMedia?.type ?: PostMediaType.NONE
        },
    )
}

private fun pickImage(seed: Long): DemoMedia =
    DemoMedia(
        url = pickBySeed(seed, demoImages),
        type = PostMediaType.IMAGE,
    )

private fun pickVideo(seed: Long): DemoMedia =
    DemoMedia(
        url = pickBySeed(seed, demoVideos),
        type = PostMediaType.VIDEO,
    )

private fun <T> pickById(id: Long, values: List<T>): T =
    values[((id - 1).absoluteValue % values.size).toInt()]

private fun <T> pickBySeed(seed: Long, values: List<T>): T =
    values[(seed.absoluteValue % values.size).toInt()]
