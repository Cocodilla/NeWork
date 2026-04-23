package ru.netology.nework.util

import android.content.Context
import android.net.Uri
import java.io.File

fun Uri.toTempFile(context: Context): File {
    val inputStream = context.contentResolver.openInputStream(this)
        ?: throw IllegalArgumentException("Cannot open input stream for uri: $this")

    val tempFile = File.createTempFile("upload_", null, context.cacheDir)

    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return tempFile
}