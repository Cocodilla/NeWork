package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.AttachmentDto
import ru.netology.nework.data.repository.EventsRepository
import ru.netology.nework.data.repository.MediaRepository
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.Coordinates
import ru.netology.nework.model.Event
import ru.netology.nework.model.EventType
import ru.netology.nework.model.PostMediaType

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val eventsRepository: EventsRepository,
    private val usersRepository: UsersRepository,
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        EventEditorState(
            datetime = defaultEventDateTime(),
            type = EventType.ONLINE,
        )
    )
    val state: StateFlow<EventEditorState> = _state.asStateFlow()

    private var originalEvent: Event? = null
    private var loadedEventId: Long? = null

    init {
        loadAvailableUsers()
    }

    fun load(eventId: Long) {
        if (eventId == 0L || loadedEventId == eventId) return
        loadedEventId = eventId

        viewModelScope.launch {
            val event = eventsRepository.getById(eventId) ?: return@launch
            originalEvent = event
            _state.value = EventEditorState(
                id = event.id,
                content = event.content,
                attachment = null,
                existingMediaUrl = event.mediaUrl,
                existingMediaType = event.mediaType,
                datetime = event.datetime,
                type = event.type,
                speakerIds = event.speakerIds,
                coordinates = event.coordinates,
                availableUsers = _state.value.availableUsers,
                saving = false,
            )
        }
    }

    fun changeContent(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun changeDateTime(value: String) {
        _state.update { it.copy(datetime = value) }
    }

    fun changeType(type: EventType) {
        _state.update { current ->
            current.copy(
                type = type,
                coordinates = if (type == EventType.ONLINE) null else current.coordinates,
            )
        }
    }

    fun changeAttachment(attachment: AttachmentModel?) {
        _state.update { it.copy(attachment = attachment) }
    }

    fun clearAttachment() {
        _state.update {
            it.copy(
                attachment = null,
                existingMediaUrl = null,
                existingMediaType = PostMediaType.NONE,
            )
        }
    }

    fun changeSpeakerIds(speakerIds: List<Long>) {
        _state.update { it.copy(speakerIds = speakerIds) }
    }

    fun changeCoordinates(coordinates: Coordinates?) {
        _state.update {
            it.copy(
                coordinates = coordinates,
                type = if (coordinates != null) EventType.OFFLINE else it.type,
            )
        }
    }

    fun clearCoordinates() {
        _state.update { it.copy(coordinates = null) }
    }

    fun save(onSaved: () -> Unit) = viewModelScope.launch {
        val currentState = _state.value
        val content = currentState.content.trim()
        if (content.isEmpty() || currentState.saving) return@launch
        if (!appAuth.authState.first().authorized) return@launch

        _state.update { it.copy(saving = true) }

        runCatching {
            val uploadedAttachment = uploadAttachment(currentState.attachment)
            val baseEvent = originalEvent ?: buildNewEvent(content)
            eventsRepository.save(
                baseEvent.copy(
                    id = currentState.id,
                    content = content,
                    datetime = currentState.datetime.ifBlank { defaultEventDateTime() },
                    type = currentState.type,
                    speakerIds = currentState.speakerIds,
                    mediaUrl = uploadedAttachment?.url ?: currentState.existingMediaUrl,
                    mediaType = uploadedAttachment?.toPostMediaType() ?: currentState.existingMediaType,
                    coordinates = currentState.coordinates.takeIf { currentState.type == EventType.OFFLINE },
                )
            )
        }.onSuccess { savedEvent ->
            originalEvent = savedEvent
            _state.update { it.copy(id = savedEvent.id, saving = false) }
            onSaved()
        }.onFailure {
            _state.update { it.copy(saving = false) }
        }
    }

    private suspend fun uploadAttachment(attachment: AttachmentModel?): AttachmentDto? {
        if (attachment == null) return null
        val uri = attachment.uri ?: return null
        val type = attachment.type ?: return null
        return mediaRepository.upload(
            uri = uri,
            fileName = attachment.name ?: "upload.bin",
            mimeType = attachment.mimeType,
            type = type,
        )
    }

    private suspend fun buildNewEvent(content: String): Event {
        val authState = appAuth.authState.first()
        val user = if (authState.id != 0L) {
            usersRepository.getById(authState.id)
        } else {
            null
        }

        return Event(
            id = 0L,
            authorId = user?.id ?: authState.id,
            author = user?.name ?: "Вы",
            authorAvatar = user?.avatar,
            authorJob = user?.job,
            content = content,
            published = LocalDateTime.now().format(PUBLISH_DATE_FORMATTER),
            datetime = _state.value.datetime.ifBlank { defaultEventDateTime() },
            type = _state.value.type,
            likedByMe = false,
            likeOwnerIds = emptyList(),
            likes = 0,
            link = null,
            ownedByMe = true,
            speakerIds = _state.value.speakerIds,
            participantsIds = emptyList(),
            participatedByMe = false,
            mediaUrl = null,
            mediaType = PostMediaType.NONE,
            coordinates = _state.value.coordinates.takeIf { _state.value.type == EventType.OFFLINE },
        )
    }

    private fun loadAvailableUsers() {
        viewModelScope.launch {
            runCatching { usersRepository.getAll() }
                .onSuccess { users ->
                    _state.update { it.copy(availableUsers = users) }
                }
        }
    }

    private fun AttachmentDto.toPostMediaType(): PostMediaType = runCatching {
        PostMediaType.valueOf(type.uppercase(Locale.ROOT))
    }.getOrDefault(PostMediaType.NONE)

    private companion object {
        val PUBLISH_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

        val EVENT_DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        fun defaultEventDateTime(): String =
            LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(EVENT_DATE_TIME_FORMATTER)
    }
}
