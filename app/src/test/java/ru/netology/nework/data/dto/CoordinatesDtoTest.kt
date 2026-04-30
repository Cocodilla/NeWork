package ru.netology.nework.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.netology.nework.model.Coordinates

class CoordinatesDtoTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `coordinates serialize with long field`() {
        val dto = Coordinates(lat = 55.75222, lng = 37.61556).toDto()

        val encoded = json.encodeToString(CoordinatesDto.serializer(), dto)

        assertTrue(encoded.contains("\"long\":37.61556"))
        assertTrue(encoded.contains("\"lat\":55.75222"))
    }

    @Test
    fun `coordinates deserialize from long field`() {
        val decoded = json.decodeFromString(
            CoordinatesDto.serializer(),
            """{"lat":55.75222,"long":37.61556}""",
        )

        assertEquals(55.75222, decoded.toModel().lat, 0.0)
        assertEquals(37.61556, decoded.toModel().lng, 0.0)
    }
}
