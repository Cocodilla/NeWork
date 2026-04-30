package ru.netology.nework.ui.auth

import androidx.annotation.StringRes

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val loading: Boolean = false,
    @StringRes val loginError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val authError: Boolean = false,
)
