package ru.netology.nework.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.netology.nework.data.repository.AuthRepository
import ru.netology.nework.model.PhotoModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    val authState = repository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ru.netology.nework.auth.AuthState(),
    )

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun changeLogin(value: String) {
        _loginState.value = _loginState.value.copy(login = value, loginError = null, authError = false)
    }

    fun changePassword(value: String) {
        _loginState.value = _loginState.value.copy(password = value, passwordError = null, authError = false)
    }

    fun signIn() {
        val state = _loginState.value
        val loginError = if (state.login.isBlank()) "Логин не может быть пустым" else null
        val passwordError = if (state.password.isBlank()) "Пароль не может быть пустым" else null
        if (loginError != null || passwordError != null) {
            _loginState.value = state.copy(loginError = loginError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _loginState.value = state.copy(loading = true, authError = false)
            try {
                repository.login(state.login.trim(), state.password)
                _loginState.value = LoginUiState()
                _events.send(AuthEvent.Authorized)
            } catch (e: HttpException) {
                _loginState.value = state.copy(loading = false, authError = e.code() == 400)
                _events.send(AuthEvent.WrongCredentials)
            } catch (_: Exception) {
                _loginState.value = state.copy(loading = false)
                _events.send(AuthEvent.UnknownError)
            }
        }
    }

    fun changeRegisterLogin(value: String) {
        _registerState.value = _registerState.value.copy(login = value, loginError = null, registerError = false)
    }

    fun changeRegisterName(value: String) {
        _registerState.value = _registerState.value.copy(name = value, nameError = null, registerError = false)
    }

    fun changeRegisterPassword(value: String) {
        _registerState.value = _registerState.value.copy(password = value, passwordError = null, registerError = false)
    }

    fun changeRepeatPassword(value: String) {
        _registerState.value = _registerState.value.copy(repeatPassword = value, repeatPasswordError = null, registerError = false)
    }

    fun changePhoto(photo: PhotoModel) {
        _registerState.value = _registerState.value.copy(photo = photo, photoError = null)
    }

    fun register() {
        val state = _registerState.value
        val loginError = if (state.login.isBlank()) "Логин не может быть пустым" else null
        val nameError = if (state.name.isBlank()) "Имя не может быть пустым" else null
        val passwordError = if (state.password.isBlank()) "Пароль не может быть пустым" else null
        val repeatPasswordError = when {
            state.repeatPassword.isBlank() -> "Повторите пароль"
            state.password != state.repeatPassword -> "Пароли не совпадают"
            else -> null
        }

        if (loginError != null || nameError != null || passwordError != null || repeatPasswordError != null) {
            _registerState.value = state.copy(
                loginError = loginError,
                nameError = nameError,
                passwordError = passwordError,
                repeatPasswordError = repeatPasswordError,
            )
            return
        }

        viewModelScope.launch {
            _registerState.value = state.copy(loading = true, registerError = false)
            try {
                repository.register(
                    login = state.login.trim(),
                    name = state.name.trim(),
                    password = state.password,
                    avatarPath = state.photo.path,
                )
                _registerState.value = RegisterUiState()
                _events.send(AuthEvent.Authorized)
            } catch (e: HttpException) {
                _registerState.value = state.copy(loading = false, registerError = e.code() == 400)
                _events.send(AuthEvent.UserAlreadyExists)
            } catch (_: Exception) {
                _registerState.value = state.copy(loading = false)
                _events.send(AuthEvent.UnknownError)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _events.send(AuthEvent.LoggedOut)
        }
    }
}

sealed interface AuthEvent {
    data object Authorized : AuthEvent
    data object LoggedOut : AuthEvent
    data object WrongCredentials : AuthEvent
    data object UserAlreadyExists : AuthEvent
    data object UnknownError : AuthEvent
}
