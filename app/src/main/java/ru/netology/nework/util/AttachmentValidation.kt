package ru.netology.nework.util

object AttachmentValidation {
    const val MAX_ATTACHMENT_SIZE_BYTES: Long = 15L * 1024L * 1024L

    fun validateSize(sizeBytes: Long): String? {
        return if (sizeBytes > MAX_ATTACHMENT_SIZE_BYTES) {
            "Размер вложения не должен превышать 15 МБ"
        } else {
            null
        }
    }
}
