package ru.netology.nework.data.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import ru.netology.nework.model.Coordinates
import ru.netology.nework.model.Event
import ru.netology.nework.model.EventType
import ru.netology.nework.model.Post
import ru.netology.nework.model.PostMediaType

class RequestDtoTest {

    @Test
    fun `post maps to request dto without server only fields`() {
        val post = Post(
            id = 7,
            authorId = 42,
            author = "User",
            authorAvatar = "avatar",
            authorJob = "QA",
            content = "Hello",
            published = "25.04.2026 09:26",
            likedByMe = true,
            likeOwnerIds = listOf(1, 2),
            likes = 2,
            link = "",
            ownedByMe = true,
            mentionIds = listOf(3, 4),
            mediaUrl = "https://server/media.jpg",
            mediaType = PostMediaType.IMAGE,
            coordinates = Coordinates(55.75, 37.61),
        )

        val dto = post.toRequestDto()
        val coords = dto.coords

        assertNotNull(coords)

        assertEquals(7L, dto.id)
        assertEquals("Hello", dto.content)
        assertEquals(listOf(3L, 4L), dto.mentionIds)
        assertNull(dto.link)
        assertEquals("https://server/media.jpg", dto.attachment?.url)
        assertEquals("IMAGE", dto.attachment?.type)
        assertEquals(55.75, coords!!.lat, 0.0)
        assertEquals(37.61, coords.long, 0.0)
    }

    @Test
    fun `event maps to request dto with normalized transport fields`() {
        val event = Event(
            id = 11,
            authorId = 42,
            author = "User",
            authorAvatar = null,
            authorJob = null,
            content = "Meetup",
            published = "ignored",
            datetime = "2026-04-25T02:26:52.163855180Z",
            type = EventType.OFFLINE,
            likedByMe = false,
            likeOwnerIds = listOf(5, 6),
            likes = 2,
            link = "https://event",
            ownedByMe = true,
            speakerIds = listOf(7, 8),
            participantsIds = listOf(9, 10),
            participatedByMe = false,
            mediaUrl = "https://server/video.mp4",
            mediaType = PostMediaType.VIDEO,
            coordinates = Coordinates(59.93, 30.31),
        )

        val dto = event.toRequestDto()
        val coords = dto.coords

        assertNotNull(coords)

        assertEquals(11L, dto.id)
        assertEquals("Meetup", dto.content)
        assertEquals("2026-04-25T02:26:52.163855180Z", dto.datetime)
        assertEquals("OFFLINE", dto.type)
        assertEquals(listOf(7L, 8L), dto.speakerIds)
        assertEquals("https://event", dto.link)
        assertEquals("https://server/video.mp4", dto.attachment?.url)
        assertEquals("VIDEO", dto.attachment?.type)
        assertEquals(59.93, coords!!.lat, 0.0)
        assertEquals(30.31, coords.long, 0.0)
    }
}
