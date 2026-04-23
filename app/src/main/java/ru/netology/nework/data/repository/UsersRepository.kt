package ru.netology.nework.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import ru.netology.nework.data.api.ApiService
import ru.netology.nework.data.dto.JobDto
import ru.netology.nework.data.dto.PostDto
import ru.netology.nework.data.dto.UserDto
import ru.netology.nework.data.dto.toDto
import ru.netology.nework.model.PostMediaType
import ru.netology.nework.model.Job
import ru.netology.nework.model.Post
import ru.netology.nework.model.User

@Singleton
class UsersRepository @Inject constructor(
    private val apiService: ApiService,
) {
    private val jobCache = mutableMapOf<Long, MutableList<Job>>()

    suspend fun getAll(): List<User> {
        val fallback = fallbackUsers()
        val users = runCatching {
            apiService.getUsers()
            .map { dto: UserDto -> dto.toModel() }
            .takeIf { it.isNotEmpty() }
            ?: fallback
        }.getOrElse {
            fallback
        }

        return users.map { user ->
            enrichUserDemo(
                user = user,
                fallback = fallback.firstOrNull { it.id == user.id || it.login == user.login },
            )
        }
    }

    suspend fun getById(id: Long): User? {
        return getAll().firstOrNull { user -> user.id == id }
    }

    suspend fun getWallByUserId(id: Long): List<Post> {
        val users = getAll()
        val wall = runCatching {
            apiService.getUserWall(id)
            .map { dto: PostDto -> dto.toModel() }
            .takeIf { it.isNotEmpty() }
            ?: fallbackWallByUserId(id)
        }.getOrElse {
            fallbackWallByUserId(id)
        }

        return wall.mapIndexed { index, post ->
            enrichPostDemo(
                post = post,
                users = users,
                index = index,
            )
        }
    }

    suspend fun getJobsByUserId(id: Long): List<Job> = runCatching {
        jobCache[id]?.toList() ?: apiService.getJobs(id)
            .map { dto: JobDto -> dto.toModel() }
            .takeIf { it.isNotEmpty() }
            ?.also { jobs -> jobCache[id] = jobs.toMutableList() }
            ?: fallbackJobs(id).also { jobs ->
                jobCache[id] = jobs.toMutableList()
            }
    }.getOrElse {
        jobCache[id]?.toList() ?: fallbackJobs(id).also { jobs ->
            jobCache[id] = jobs.toMutableList()
        }
    }

    suspend fun saveJob(userId: Long, job: Job): Job {
        val currentJobs = (jobCache[userId]?.toMutableList() ?: getJobsByUserId(userId).toMutableList())
        val fallbackJob = job.copy(
            id = if (job.id == 0L) nextJobId(currentJobs) else job.id,
            finish = job.finish?.takeIf { it.isNotBlank() },
            link = job.link?.takeIf { it.isNotBlank() },
        )
        val saved = runCatching {
            apiService.saveJob(job.toDto()).toModel()
        }.getOrElse {
            fallbackJob
        }

        val index = currentJobs.indexOfFirst { it.id == saved.id }
        if (index == -1) {
            currentJobs.add(0, saved)
        } else {
            currentJobs[index] = saved
        }
        jobCache[userId] = currentJobs
        return saved
    }

    suspend fun removeJob(userId: Long, jobId: Long) {
        val currentJobs = (jobCache[userId]?.toMutableList() ?: getJobsByUserId(userId).toMutableList())
        runCatching {
            apiService.removeJob(jobId)
        }
        jobCache[userId] = currentJobs.filterNot { it.id == jobId }.toMutableList()
    }

    private fun fallbackUsers(): List<User> = listOf(
        User(
            id = 1,
            login = "igummera8",
            name = "Adison Levin",
            avatar = "https://i.pravatar.cc/300?u=adison",
            job = "iOS Developer",
            about = "Люблю быстрые интерфейсы и аккуратные мобильные детали.",
        ),
        User(
            id = 2,
            login = "spinjury",
            name = "Livia Donin",
            avatar = "https://i.pravatar.cc/300?u=livia",
            job = "Android Engineer",
            about = "Собираю стабильные Android-экраны и люблю чистую навигацию.",
        ),
        User(
            id = 3,
            login = "lbragancapx",
            name = "Lydia Westervelt",
            avatar = null,
            job = "Product Designer",
            about = "Проектирую спокойные интерфейсы и визуальные сценарии.",
        ),
        User(
            id = 4,
            login = "smorrill8l",
            name = "Kaylynn Mango",
            avatar = "https://i.pravatar.cc/300?u=kaylynn",
            job = "Event Host",
            about = "Организую события и люблю собирать людей вокруг сильной идеи.",
        ),
        User(
            id = 5,
            login = "tokenny8q",
            name = "Leo Lipshutz",
            avatar = "https://i.pravatar.cc/300?u=leo",
            job = "QA Lead",
            about = "Нахожу углы, которые обычно никто не замечает.",
        ),
        User(
            id = 6,
            login = "afruen83",
            name = "Vito Dobbs",
            avatar = "https://i.pravatar.cc/300?u=vito",
            job = "Speaker",
            about = "Провожу лекции и люблю упаковывать сложное в понятные истории.",
        ),
        User(
            id = 7,
            login = "thenaughannt",
            name = "Dulce Kenter",
            avatar = "https://i.pravatar.cc/300?u=dulce",
            job = "Community Manager",
            about = "Развиваю комьюнити и выстраиваю добрые продуктовые привычки.",
        ),
        User(
            id = 8,
            login = "gprends",
            name = "Lydia Levin",
            avatar = "https://i.pravatar.cc/300?u=lydialevin",
            job = "Motion Designer",
            about = "Занимаюсь анимацией и добавляю экранам ритм.",
        ),
        User(
            id = 9,
            login = "fsukbhansf2",
            name = "Ryan Dias",
            avatar = "https://i.pravatar.cc/300?u=ryandias",
            job = "Backend Engineer",
            about = "Собираю API и люблю, когда данные приходят вовремя.",
        ),
        User(
            id = 10,
            login = "charliex4",
            name = "Charlie Lipshutz",
            avatar = null,
            job = "DevRel",
            about = "Связываю команду, продукт и разработчиков.",
        ),
    )

    private fun fallbackWallByUserId(userId: Long): List<Post> {
        val user = fallbackUsers().firstOrNull { it.id == userId } ?: return emptyList()

        return listOf(
            Post(
                id = user.id * 100 + 1,
                authorId = user.id,
                author = user.name,
                authorAvatar = user.avatar,
                authorJob = user.job,
                content = "Для разгрузки вагонов с углем необходимо подготовить место, снять стороннее устройство, выгрузить уголь, переместить на другое место, закрыть вагон, установить стопорное устройство и проверить состояние груза перед следующим вагоном.",
                published = "11.05.2022 11:21",
                likedByMe = false,
                likes = 2,
                link = null,
                ownedByMe = false,
                mentionIds = emptyList(),
                mediaUrl = null,
                mediaType = PostMediaType.NONE,
                coordinates = null,
            ),
            Post(
                id = user.id * 100 + 2,
                authorId = user.id,
                author = user.name,
                authorAvatar = user.avatar,
                authorJob = user.job,
                content = "Копание в мокрой земле может быть довольно неприятным и грязным занятием. Однако, если вы хотите найти что-то ценное или просто заняться садоводством, то копание в мокрой земле может стать интересным опытом.",
                published = "21.02.2022 14:23",
                likedByMe = false,
                likes = 10,
                link = null,
                ownedByMe = false,
                mentionIds = emptyList(),
                mediaUrl = "https://images.unsplash.com/photo-1466692476868-aef1dfb1e735?auto=format&fit=crop&w=1200&q=80",
                mediaType = PostMediaType.IMAGE,
                coordinates = null,
            ),
        )
    }

    private fun fallbackJobs(userId: Long): List<Job> {
        return when (userId) {
            1L -> listOf(
                Job(
                    id = 11,
                    name = "ООО Элтекс",
                    position = "Преподаватель",
                    start = "1 апреля 2023",
                    finish = null,
                    link = null,
                ),
                Job(
                    id = 12,
                    name = "ПАО ПСБ",
                    position = "Главный инженер-программист",
                    start = "1 августа 2020",
                    finish = null,
                    link = "https://m2.material.io/components/cards",
                ),
                Job(
                    id = 13,
                    name = "ГК Центр Финансовых Технологий",
                    position = "Инженер-программист",
                    start = "1 марта 2019",
                    finish = "1 августа 2020",
                    link = null,
                ),
            )

            else -> {
                val user = fallbackUsers().firstOrNull { it.id == userId }
                val currentPosition = user?.job ?: "Product Specialist"
                listOf(
                    Job(
                        id = userId * 10 + 1,
                        name = "NeWork",
                        position = currentPosition,
                        start = "2023-09-01",
                        finish = null,
                        link = "https://netology.ru",
                    ),
                    Job(
                        id = userId * 10 + 2,
                        name = "Community Lab",
                        position = "Project Contributor",
                        start = "2021-03-01",
                        finish = "2023-08-31",
                        link = null,
                    ),
                )
            }
        }
    }

    private fun nextJobId(jobs: List<Job>): Long =
        (jobs.maxOfOrNull { it.id } ?: 0L) + 1L

    private fun fallbackJobs(): List<Job> = listOf(
        Job(
            id = 1,
            name = "Netology",
            position = "Android Developer",
            start = "2023-09-01",
            finish = null,
            link = "https://netology.ru",
        )
    )
}
