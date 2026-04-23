package ru.netology.nework.data.dto

import kotlinx.serialization.Serializable
import ru.netology.nework.model.Job

@Serializable
data class JobDto(
    val id: Long = 0,
    val name: String = "",
    val position: String = "",
    val start: String = "",
    val finish: String? = null,
    val link: String? = null,
) {
    fun toModel(): Job = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link,
    )
}

fun Job.toDto(): JobDto = JobDto(
    id = id,
    name = name,
    position = position,
    start = start,
    finish = finish,
    link = link,
)
