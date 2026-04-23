package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.AttachmentDto
import ru.netology.nework.data.dto.EventDto
import ru.netology.nework.data.dto.toDto
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.model.Event
import ru.netology.nework.model.EventType
import ru.netology.nework.model.PostMediaType

@Singleton
class EventsRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    suspend fun getAll(): List<Event> {
        val loaded: List<Event> = runCatching {
            apiService.getEvents()
                .map { dto: EventDto -> dto.toModel() }
                .takeIf { it.isNotEmpty() }
                ?: fallbackEvents()
        }.getOrElse {
            fallbackEvents()
        }
        _events.value = loaded
        return loaded
    }

    suspend fun getById(id: Long): Event? {
        val current = _events.value
        return current.firstOrNull { event -> event.id == id }
            ?: getAll().firstOrNull { event -> event.id == id }
    }

    suspend fun save(event: Event): Event {
        val request = event.toDto()
        val saved: Event = runCatching {
            apiService.saveEvent(request).toModel()
        }.getOrElse {
            if (event.id == 0L) event.copy(id = nextId()) else event
        }

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
        runCatching {
            apiService.removeEvent(id)
        }
        _events.value = _events.value.filterNot { event -> event.id == id }
    }

    suspend fun likeById(id: Long): Event? {
        val current = _events.value.firstOrNull { event -> event.id == id } ?: return null

        val updated: Event = runCatching {
            if (current.likedByMe) {
                apiService.dislikeEvent(id).toModel()
            } else {
                apiService.likeEvent(id).toModel()
            }
        }.getOrElse {
            val likeOwnerIds = buildLikeOwnerIdsFallback(current)
            current.copy(
                likedByMe = !current.likedByMe,
                likeOwnerIds = likeOwnerIds,
                likes = likeOwnerIds.size,
            )
        }

        _events.value = _events.value.map { event ->
            if (event.id == id) updated else event
        }
        return updated
    }

    suspend fun participateById(id: Long): Event? {
        val current = _events.value.firstOrNull { event -> event.id == id } ?: return null

        val updated: Event = runCatching {
            if (current.participatedByMe) {
                apiService.unparticipateEvent(id).toModel()
            } else {
                apiService.participateEvent(id).toModel()
            }
        }.getOrElse {
            val participantsIds = buildParticipantsIdsFallback(current)
            current.copy(
                participantsIds = participantsIds,
                participatedByMe = !current.participatedByMe,
            )
        }

        _events.value = _events.value.map { event ->
            if (event.id == id) updated else event
        }
        return updated
    }

    private fun nextId(): Long = (_events.value.maxOfOrNull { it.id } ?: 0L) + 1L

    private fun fallbackEvents(): List<Event> = listOf(
        Event(
            id = 1,
            authorId = 3,
            author = "Lydia Westervelt",
            authorAvatar = null,
            authorJob = "Product Designer",
            content = "Приглашаю провести уютный вечер за увлекательными играми! У нас есть несколько вариантов настолок, подходящих для любой компании.",
            published = "11.05.2022 11:21",
            datetime = "16.05.2022 12:00",
            type = EventType.OFFLINE,
            likedByMe = false,
            likeOwnerIds = listOf(1, 5),
            likes = 2,
            link = "https://m2.material.io/components/cards",
            ownedByMe = true,
            speakerIds = listOf(1, 2, 7),
            participantsIds = listOf(1, 2, 4, 7),
            participatedByMe = false,
            mediaUrl = null,
            mediaType = PostMediaType.NONE,
            coordinates = ru.netology.nework.model.Coordinates(55.75586, 37.61770),
        ),
        Event(
            id = 2,
            authorId = 2,
            author = "Livia Donin",
            authorAvatar = null,
            authorJob = "Android Engineer",
            content = "Приглашаем всех желающих на увлекательный урок груминга! Наш преподаватель покажет вам основные приемы этого выразительного языка тела и научит правильно общаться и контролировать голос.",
            published = "20.06.2022 08:59",
            datetime = "20.06.2022 14:00",
            type = EventType.ONLINE,
            likedByMe = false,
            likeOwnerIds = listOf(1, 2, 4, 5, 6, 7, 8, 9, 10, 3),
            likes = 10,
            link = null,
            ownedByMe = false,
            speakerIds = listOf(1, 4, 7),
            participantsIds = listOf(1, 2, 3, 4, 5, 7),
            participatedByMe = false,
            mediaUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1200&q=80",
            mediaType = PostMediaType.VIDEO,
            coordinates = null,
        ),
        Event(
            id = 3,
            authorId = 1,
            author = "Adison Levin",
            authorAvatar = null,
            authorJob = "iOS Developer",
            content = "Приглашаем вас на увлекательную лекцию о том, как правильно дрессировать котиков! Опытный кинолог расскажет вам о различных техниках и методах обучения, поделится забавными случаями из практики и ответит на ваши вопросы.",
            published = "21.08.2022 14:23",
            datetime = "28.08.2022 19:00",
            type = EventType.ONLINE,
            likedByMe = false,
            likeOwnerIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            likes = 10,
            link = null,
            ownedByMe = false,
            speakerIds = listOf(2, 6, 7),
            participantsIds = listOf(2, 3, 6, 7, 8),
            participatedByMe = false,
            mediaUrl = "https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=1200&q=80",
            mediaType = PostMediaType.IMAGE,
            coordinates = null,
        ),
    )

    private suspend fun buildLikeOwnerIdsFallback(event: Event): List<Long> {
        val currentUserId = appAuth.authState.first().id
        return if (event.likedByMe) {
            event.likeOwnerIds.filterNot { ownerId -> ownerId == currentUserId }
        } else {
            event.likeOwnerIds + currentUserId.takeIf { it != 0L }.orEmptyFallback(event.authorId)
        }.distinct()
    }

    private suspend fun buildParticipantsIdsFallback(event: Event): List<Long> {
        val currentUserId = appAuth.authState.first().id
        if (currentUserId == 0L) return event.participantsIds

        return if (event.participatedByMe) {
            event.participantsIds.filterNot { participantId -> participantId == currentUserId }
        } else {
            (event.participantsIds + currentUserId).distinct()
        }
    }
}

private fun Event.toDto(): EventDto = EventDto(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published,
    datetime = datetime,
    type = type.name,
    likedByMe = likedByMe,
    likeOwnerIds = likeOwnerIds,
    speakerIds = speakerIds,
    participantsIds = participantsIds,
    participatedByMe = participatedByMe,
    link = link,
    ownedByMe = ownedByMe,
    attachment = mediaUrl?.takeIf { mediaType != PostMediaType.NONE }?.let { url ->
        AttachmentDto(
            url = url,
            type = mediaType.name,
        )
    },
    coords = coordinates?.toDto(),
)

private fun Long?.orEmptyFallback(fallback: Long): Long = this ?: fallback
