package ru.netology.nework.util

import android.net.Uri
import ru.netology.nework.BuildConfig

private val serverOrigin: String by lazy {
    val baseUri = Uri.parse(BuildConfig.BASE_URL.removeSuffix("/"))
    buildString {
        append(baseUri.scheme ?: "http")
        append("://")
        append(baseUri.host.orEmpty())
        if (baseUri.port != -1) {
            append(":")
            append(baseUri.port)
        }
    }.trimEnd('/')
}

fun String?.toServerUrlOrNull(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    return if (value.startsWith("/")) "$serverOrigin$value" else "$serverOrigin/$value"
}
