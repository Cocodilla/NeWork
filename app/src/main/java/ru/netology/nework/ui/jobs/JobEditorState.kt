package ru.netology.nework.ui.jobs

data class JobEditorState(
    val id: Long = 0L,
    val name: String = "",
    val position: String = "",
    val link: String = "",
    val start: String = "",
    val finish: String = "",
) {
    val isValid: Boolean
        get() = name.isNotBlank() && position.isNotBlank() && start.isNotBlank()
}
