package ru.netology.nework.data.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nework.data.dto.EventDto
import ru.netology.nework.data.dto.JobDto
import ru.netology.nework.data.dto.MediaUploadResponse
import ru.netology.nework.data.dto.PostDto
import ru.netology.nework.data.dto.UserDto

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @POST("posts")
    suspend fun savePost(@Body body: PostDto): PostDto

    @DELETE("posts/{id}")
    suspend fun removePost(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likePost(@Path("id") id: Long): PostDto

    @DELETE("posts/{id}/likes")
    suspend fun dislikePost(@Path("id") id: Long): PostDto

    @GET("events")
    suspend fun getEvents(): List<EventDto>

    @POST("events")
    suspend fun saveEvent(@Body body: EventDto): EventDto

    @DELETE("events/{id}")
    suspend fun removeEvent(@Path("id") id: Long)

    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Long): EventDto

    @DELETE("events/{id}/likes")
    suspend fun dislikeEvent(@Path("id") id: Long): EventDto

    @POST("events/{id}/participants")
    suspend fun participateEvent(@Path("id") id: Long): EventDto

    @DELETE("events/{id}/participants")
    suspend fun unparticipateEvent(@Path("id") id: Long): EventDto

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("{authorId}/wall")
    suspend fun getUserWall(@Path("authorId") authorId: Long): List<PostDto>

    @GET("{authorId}/jobs")
    suspend fun getJobs(@Path("authorId") authorId: Long): List<JobDto>

    @POST("my/jobs")
    suspend fun saveJob(@Body body: JobDto): JobDto

    @DELETE("my/jobs/{id}")
    suspend fun removeJob(@Path("id") id: Long)

    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
    ): MediaUploadResponse
}
