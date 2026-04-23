package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.User

@Serializable
data class UserDto(
    val id: Long = 0,
    val login: String = "",
    val name: String = "",
    val avatar: String? = null,
) {
    fun toModel(): User = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,
        job = null,
        about = null,
    )
}
