package ru.netology.nework.util

import ru.netology.nework.model.Coordinates
import java.util.Locale

fun Coordinates.toDisplayString(): String =
    String.format(Locale.US, "%.5f, %.5f", lat, lng)

fun String?.toCoordinatesOrNull(): Coordinates? {
    if (this.isNullOrBlank()) return null
    val parts = split(",").map { it.trim() }
    if (parts.size != 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    return Coordinates(lat = lat, lng = lng)
}
