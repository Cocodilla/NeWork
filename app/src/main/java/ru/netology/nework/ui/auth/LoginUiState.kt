package ru.netology.nework.ui.auth

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val loginError: String? = null,
    val passwordError: String? = null,
    val authError: Boolean = false,
)
