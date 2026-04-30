package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.repository.EventsRepository
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.model.Event
import ru.netology.nework.model.User

data class EventDetailsUiState(
    val loading: Boolean = false,
    val event: Event? = null,
    val speakers: List<User> = emptyList(),
    val likers: List<User> = emptyList(),
    val participants: List<User> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: EventsRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EventDetailsUiState(loading = true))
    val state: StateFlow<EventDetailsUiState> = _state.asStateFlow()

    private var eventId: Long = 0L

    fun load(id: Long) {
        eventId = id
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val event = repository.getById(eventId)
                val users = usersRepository.getAll()
                buildUiState(event, users)
            }.onSuccess { uiState ->
                _state.value = uiState
            }.onFailure { error ->
                _state.value = EventDetailsUiState(
                    loading = false,
                    error = error.message ?: "Не удалось загрузить событие",
                )
            }
        }
    }

    fun onLike() {
        val current = _state.value.event ?: return
        viewModelScope.launch {
            runCatching {
                val updated = repository.likeById(current.id) ?: return@runCatching null
                val users = usersRepository.getAll()
                buildUiState(updated, users)
            }.onSuccess { uiState ->
                uiState?.let { _state.value = it }
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message ?: "Не удалось обновить лайк")
            }
        }
    }

    fun onParticipate() {
        val current = _state.value.event ?: return
        viewModelScope.launch {
            if (!appAuth.authState.first().authorized) return@launch
            runCatching {
                val updated = repository.participateById(current.id) ?: return@runCatching null
                val users = usersRepository.getAll()
                buildUiState(updated, users)
            }.onSuccess { uiState ->
                uiState?.let { _state.value = it }
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message ?: "Не удалось обновить участие")
            }
        }
    }

    fun onRemove(onRemoved: () -> Unit) {
        val current = _state.value.event ?: return
        viewModelScope.launch {
            runCatching {
                repository.removeById(current.id)
            }.onSuccess {
                onRemoved()
            }.onFailure { error ->
                _state.value = _state.value.copy(error = error.message ?: "Не удалось удалить событие")
            }
        }
    }

    private fun buildUiState(event: Event?, users: List<User>): EventDetailsUiState {
        if (event == null) {
            return EventDetailsUiState(
                loading = false,
                error = "Событие не найдено",
            )
        }

        val speakers = users.filter { user -> user.id in event.speakerIds }
        val likers = users.filter { user -> user.id in event.likeOwnerIds }
        val participants = users.filter { user -> user.id in event.participantsIds }

        return EventDetailsUiState(
            loading = false,
            event = event,
            speakers = speakers,
            likers = likers,
            participants = participants,
            error = null,
        )
    }
}
