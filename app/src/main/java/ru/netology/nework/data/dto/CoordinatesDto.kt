package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import ru.netology.nework.model.Coordinates

@Serializable
data class CoordinatesDto(
    val lat: Double,
    @SerialName("long")
    val long: Double,
) {
    fun toModel(): Coordinates = Coordinates(
        lat = lat,
        lng = long,
    )
}

fun Coordinates.toDto(): CoordinatesDto = CoordinatesDto(
    lat = lat,
    long = lng,
)
