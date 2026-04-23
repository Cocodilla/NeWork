package ru.netology.nework.ui.profile

import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.User

data class MyProfileUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val wall: List<Post> = emptyList(),
    val jobs: List<Job> = emptyList(),
)