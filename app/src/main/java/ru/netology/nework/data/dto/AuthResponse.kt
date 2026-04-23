package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val id: Long,
    val token: String,
)
