package ru.netology.nework.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.AttachmentDto
import ru.netology.nework.model.AttachmentType
import ru.netology.nework.util.uriToMultipartPart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
) {
    suspend fun upload(
        uri: Uri,
        fileName: String,
        mimeType: String?,
        type: AttachmentType,
    ): AttachmentDto = withContext(Dispatchers.IO) {
        val part = context.uriToMultipartPart(
            uri = uri,
            fileName = fileName,
            mimeType = mimeType,
        )
        val response = apiService.uploadMedia(part)
        AttachmentDto(
            url = response.url,
            type = type.name,
        )
    }
}
