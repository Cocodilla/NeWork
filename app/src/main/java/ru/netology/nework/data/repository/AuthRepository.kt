package ru.netology.nework.data.repository

import android.content.Context
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.data.api.AuthApiService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val appAuth: AppAuth,
    @ApplicationContext private val context: Context,
) {
    val authState: Flow<AuthState> = appAuth.authState

    suspend fun login(login: String, password: String) {
        val response = api.login(login = login, pass = password)
        appAuth.setAuth(response.id, response.token)
    }

    suspend fun register(
        login: String,
        name: String,
        password: String,
        avatarPath: String?,
    ) {
        val response = avatarPath?.let { path ->
            val file = File(path)
            val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.toMediaTypeOrNull()
                ?: "image/*".toMediaTypeOrNull()
            api.registerWithPhoto(
                login = login.toRequestBody(MultipartBody.FORM),
                pass = password.toRequestBody(MultipartBody.FORM),
                name = name.toRequestBody(MultipartBody.FORM),
                file = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody(mime),
                ),
            )
        } ?: api.register(
            login = login,
            pass = password,
            name = name,
        )
        appAuth.setAuth(response.id, response.token)
    }

    suspend fun logout() {
        appAuth.clearAuth()
    }
}
