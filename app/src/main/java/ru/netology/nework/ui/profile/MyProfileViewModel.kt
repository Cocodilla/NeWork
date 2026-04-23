package ru.netology.nework.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.repository.UsersRepository

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val repository: UsersRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _state = MutableStateFlow(MyProfileUiState())
    val state: StateFlow<MyProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching {
                val authId = appAuth.authState.first().id
                val users = repository.getAll()
                val me = when {
                    authId != 0L -> users.firstOrNull { it.id == authId } ?: users.firstOrNull()
                    else -> users.firstOrNull()
                }

                if (me == null) {
                    MyProfileUiState(
                        loading = false,
                        user = null,
                        wall = emptyList(),
                        jobs = emptyList(),
                        error = "Пользователь не найден",
                    )
                } else {
                    val wall = runCatching {
                        repository.getWallByUserId(me.id)
                    }.getOrDefault(emptyList())

                    val jobs = runCatching {
                        repository.getJobsByUserId(me.id)
                    }.getOrDefault(emptyList())

                    MyProfileUiState(
                        loading = false,
                        user = me,
                        wall = wall,
                        jobs = jobs,
                        error = null,
                    )
                }
            }.onSuccess { uiState ->
                _state.value = uiState
            }.onFailure { e ->
                _state.value = MyProfileUiState(
                    loading = false,
                    user = null,
                    wall = emptyList(),
                    jobs = emptyList(),
                    error = e.message ?: "Не удалось загрузить профиль",
                )
            }
        }
    }

    fun removeJob(jobId: Long) {
        val userId = _state.value.user?.id ?: return

        viewModelScope.launch {
            repository.removeJob(userId, jobId)
            _state.value = _state.value.copy(
                jobs = _state.value.jobs.filterNot { it.id == jobId }
            )
        }
    }
}
