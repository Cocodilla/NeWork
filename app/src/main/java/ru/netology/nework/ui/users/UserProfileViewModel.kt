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
import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.User

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UsersRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileUiState())
    val state: StateFlow<UserProfileUiState> = _state.asStateFlow()

    fun load(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching {
                val user = repository.getById(userId)

                if (user == null) {
                    UserProfileUiState(
                        loading = false,
                        user = null,
                        wall = emptyList(),
                        jobs = emptyList(),
                        error = "Пользователь не найден",
                    )
                } else {
                    val wall = runCatching {
                        repository.getWallByUserId(user.id)
                    }.getOrDefault(emptyList())

                    val jobs = runCatching {
                        repository.getJobsByUserId(user.id)
                    }.getOrDefault(emptyList())

                    UserProfileUiState(
                        loading = false,
                        user = user,
                        wall = wall,
                        jobs = jobs,
                        error = null,
                    )
                }
            }.onSuccess { uiState ->
                _state.value = uiState
            }.onFailure { e ->
                _state.value = UserProfileUiState(
                    loading = false,
                    user = null,
                    wall = emptyList(),
                    jobs = emptyList(),
                    error = e.message ?: "Не удалось загрузить профиль пользователя",
                )
            }
        }
    }
}
