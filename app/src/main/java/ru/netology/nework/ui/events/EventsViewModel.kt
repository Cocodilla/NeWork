package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.EventsRepository
import ru.netology.nework.model.Event

data class EventsUiState(
    val loading: Boolean = false,
    val data: List<Event> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EventsUiState())
    val state: StateFlow<EventsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.events.collectLatest { events ->
                _state.value = _state.value.copy(
                    loading = false,
                    data = events,
                )
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                repository.getAll()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Ошибка загрузки событий",
                )
            }
        }
    }

    fun onLike(event: Event) {
        viewModelScope.launch {
            repository.likeById(event.id)
        }
    }

    fun onDelete(eventId: Long) {
        viewModelScope.launch {
            repository.removeById(eventId)
        }
    }
}
