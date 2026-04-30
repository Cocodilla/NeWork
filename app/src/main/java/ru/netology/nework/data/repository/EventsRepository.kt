package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.EventDto
import ru.netology.nework.data.dto.toRequestDto
import ru.netology.nework.model.Event

@Singleton
class EventsRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    suspend fun getAll(): List<Event> {
        val currentUserId = appAuth.authState.first().id
        val loaded = apiService.getEvents().map { dto: EventDto -> dto.toModel(currentUserId) }
        _events.value = loaded
        return loaded
    }

    suspend fun getById(id: Long): Event? {
        val current = _events.value
        return current.firstOrNull { event -> event.id == id }
            ?: getAll().firstOrNull { event -> event.id == id }
    }

    suspend fun save(event: Event): Event {
        val currentUserId = appAuth.authState.first().id
        val saved = apiService.saveEvent(event.toRequestDto()).toModel(currentUserId)

        val current = _events.value.toMutableList()
        val index = current.indexOfFirst { it.id == saved.id }
        if (index == -1) {
            current.add(0, saved)
        } else {
            current[index] = saved
        }
        _events.value = current
        return saved
    }

    suspend fun removeById(id: Long) {
        apiService.removeEvent(id)
        _events.value = _events.value.filterNot { event -> event.id == id }
    }

    suspend fun likeById(id: Long): Event? {
        val current = _events.value.firstOrNull { event -> event.id == id } ?: return null
        val currentUserId = appAuth.authState.first().id

        val updated = if (current.likedByMe) {
            apiService.dislikeEvent(id).toModel(currentUserId)
        } else {
            apiService.likeEvent(id).toModel(currentUserId)
        }

        _events.value = _events.value.map { event ->
            if (event.id == id) updated else event
        }
        return updated
    }

    suspend fun participateById(id: Long): Event? {
        val current = _events.value.firstOrNull { event -> event.id == id } ?: return null
        val currentUserId = appAuth.authState.first().id

        val updated = if (current.participatedByMe) {
            apiService.unparticipateEvent(id).toModel(currentUserId)
        } else {
            apiService.participateEvent(id).toModel(currentUserId)
        }

        _events.value = _events.value.map { event ->
            if (event.id == id) updated else event
        }
        return updated
    }
}
