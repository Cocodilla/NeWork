package ru.netology.nework.ui.auth

import androidx.annotation.StringRes
import ru.netology.nework.model.PhotoModel

data class RegisterUiState(
    val login: String = "",
    val name: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val photo: PhotoModel = PhotoModel(),
    val loading: Boolean = false,
    @StringRes val loginError: Int? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val repeatPasswordError: Int? = null,
    val photoError: String? = null,
    val registerError: Boolean = false,
)
