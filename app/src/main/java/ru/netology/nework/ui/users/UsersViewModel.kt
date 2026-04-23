package ru.netology.nework.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.model.User
import ru.netology.nework.ui.common.UiState

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UsersRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState<User>(loading = true))
    val state: StateFlow<UiState<User>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState(loading = true)
            runCatching { repository.getAll() }
                .onSuccess { _state.value = UiState(data = it) }
                .onFailure { _state.value = UiState(error = it.message ?: "Ошибка") }
        }
    }
}
