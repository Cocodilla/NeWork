package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.JobDto
import ru.netology.nework.data.dto.PostDto
import ru.netology.nework.data.dto.UserDto
import ru.netology.nework.data.dto.toDto
import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.User

@Singleton
class UsersRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) {
    private val jobCache = mutableMapOf<Long, MutableList<Job>>()

    suspend fun getAll(): List<User> =
        apiService.getUsers().map { dto: UserDto -> dto.toModel() }

    suspend fun getById(id: Long): User? {
        return apiService.getUserById(id).toModel()
    }

    suspend fun getWallByUserId(id: Long): List<Post> {
        val currentUserId = appAuth.authState.first().id
        return apiService.getUserWall(id).map { dto: PostDto -> dto.toModel(currentUserId) }
    }

    suspend fun getJobsByUserId(id: Long): List<Job> {
        jobCache[id]?.toList()?.let { return it }

        val jobs = apiService.getJobs(id)
            .map { dto: JobDto -> dto.toModel() }
        jobCache[id] = jobs.toMutableList()
        return jobs
    }

    suspend fun saveJob(userId: Long, job: Job): Job {
        val currentJobs = jobCache[userId]?.toMutableList() ?: getJobsByUserId(userId).toMutableList()
        val saved = apiService.saveJob(job.toDto()).toModel()

        val index = currentJobs.indexOfFirst { it.id == saved.id }
        if (index == -1) {
            currentJobs.add(0, saved)
        } else {
            currentJobs[index] = saved
        }
        jobCache[userId] = currentJobs
        return saved
    }

    suspend fun removeJob(userId: Long, jobId: Long) {
        val currentJobs = jobCache[userId]?.toMutableList() ?: getJobsByUserId(userId).toMutableList()
        apiService.removeJob(jobId)
        jobCache[userId] = currentJobs.filterNot { it.id == jobId }.toMutableList()
    }
}
