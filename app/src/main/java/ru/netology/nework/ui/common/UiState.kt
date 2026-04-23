package ru.netology.nework.ui.common

data class UiState<T>(
    val loading: Boolean = false,
    val error: String? = null,
    val data: List<T> = emptyList(),
)
