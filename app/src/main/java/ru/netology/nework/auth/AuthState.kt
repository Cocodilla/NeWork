package ru.netology.nework.auth

data class AuthState(
    val id: Long = 0L,
    val token: String? = null,
) {
    val authorized: Boolean
        get() = id != 0L && !token.isNullOrBlank()
}
