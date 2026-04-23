package ru.netology.nework.ui.auth

import ru.netology.nework.model.PhotoModel

data class RegisterUiState(
    val login: String = "",
    val name: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val photo: PhotoModel = PhotoModel(),
    val loading: Boolean = false,
    val loginError: String? = null,
    val nameError: String? = null,
    val passwordError: String? = null,
    val repeatPasswordError: String? = null,
    val photoError: String? = null,
    val registerError: Boolean = false,
)
