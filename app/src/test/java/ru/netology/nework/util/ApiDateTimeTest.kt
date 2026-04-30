package ru.netology.nework.util

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiDateTimeTest {

    private val moscowZone = ZoneId.of("Europe/Moscow")

    @Test
    fun `display datetime converts to api utc`() {
        val result = "25.04.2026 09:26".toApiUtcDateTimeOrNull(moscowZone)

        assertEquals("2026-04-25T06:26:00Z", result)
    }

    @Test
    fun `iso datetime converts to display format`() {
        val result = "2026-04-25T02:26:52.163855180Z".toDisplayDateTimeOrSelf(moscowZone)

        assertEquals("25.04.2026 05:26", result)
    }

    @Test
    fun `invalid datetime returns null`() {
        val result = "25/04/2026 09:26".toApiUtcDateTimeOrNull(moscowZone)

        assertNull(result)
    }
}
