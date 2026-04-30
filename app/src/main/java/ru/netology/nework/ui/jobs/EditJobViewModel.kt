package ru.netology.nework.ui.jobs

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
import ru.netology.nework.data.repository.UsersRepository
import ru.netology.nework.model.Job
import ru.netology.nework.util.toApiUtcStartOfDayOrNull
import ru.netology.nework.util.toDisplayDateOrSelf

@HiltViewModel
class EditJobViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JobEditorState())
    val state: StateFlow<JobEditorState> = _state.asStateFlow()

    private var loadedJobId: Long? = null

    fun load(jobId: Long) {
        if (jobId == 0L) {
            loadedJobId = 0L
            _state.value = JobEditorState()
            return
        }
        if (loadedJobId == jobId) return
        loadedJobId = jobId

        viewModelScope.launch {
            val me = resolveCurrentUserId() ?: return@launch
            val job = usersRepository.getJobsByUserId(me).firstOrNull { it.id == jobId } ?: return@launch
            _state.value = JobEditorState(
                id = job.id,
                name = job.name,
                position = job.position,
                link = job.link.orEmpty(),
                start = job.start.toDisplayDateOrSelf(),
                finish = job.finish.orEmpty().toDisplayDateOrSelf(),
            )
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onPositionChange(value: String) {
        _state.value = _state.value.copy(position = value)
    }

    fun onLinkChange(value: String) {
        _state.value = _state.value.copy(link = value)
    }

    fun onStartChange(value: String) {
        _state.value = _state.value.copy(start = value)
    }

    fun onFinishChange(value: String) {
        _state.value = _state.value.copy(finish = value)
    }

    fun save(onSaved: () -> Unit) {
        val currentState = _state.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            val me = resolveCurrentUserId() ?: return@launch
            usersRepository.saveJob(
                userId = me,
                job = Job(
                    id = currentState.id,
                    name = currentState.name.trim(),
                    position = currentState.position.trim(),
                    start = currentState.start
                        .trim()
                        .toApiUtcStartOfDayOrNull()
                        ?: return@launch,
                    finish = currentState.finish
                        .trim()
                        .takeIf { it.isNotBlank() }
                        ?.toApiUtcStartOfDayOrNull(),
                    link = currentState.link.trim().takeIf { it.isNotBlank() },
                )
            )
            onSaved()
        }
    }

    private suspend fun resolveCurrentUserId(): Long? =
        appAuth.authState.first().id.takeIf { it != 0L }
}
