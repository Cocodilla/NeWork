package ru.netology.nework.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.netology.nework.data.dto.AuthResponse

interface AuthApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): AuthResponse

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part? = null,
    ): AuthResponse
}
