package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Coordinates

@Serializable
data class CoordinatesDto(
    val lat: Double,
    val lng: Double,
) {
    fun toModel(): Coordinates = Coordinates(
        lat = lat,
        lng = lng,
    )
}

fun Coordinates.toDto(): CoordinatesDto = CoordinatesDto(
    lat = lat,
    lng = lng,
)
