package ru.netology.nework.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun Context.uriToMultipartPart(
    uri: Uri,
    partName: String = "file",
    fileName: String = "upload.bin",
    mimeType: String? = null,
): MultipartBody.Part {
    val tempFile = File.createTempFile("nework_upload_", null, cacheDir)
    contentResolver.openInputStream(uri).use { input ->
        FileOutputStream(tempFile).use { output ->
            input?.copyTo(output)
        }
    }

    val requestBody = tempFile.asRequestBody(mimeType?.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}
