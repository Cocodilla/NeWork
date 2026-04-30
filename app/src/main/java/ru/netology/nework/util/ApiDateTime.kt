package ru.netology.nework.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DisplayDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

private val DisplayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

private val LocalDateTimeParsers = listOf(
    DisplayDateTimeFormatter,
    DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
)

private val LocalDateParsers = listOf(
    DisplayDateFormatter,
    DateTimeFormatter.ISO_LOCAL_DATE,
)

fun defaultEventDateTimeValue(zoneId: ZoneId = ZoneId.systemDefault()): String =
    LocalDateTime.now(zoneId)
        .plusDays(1)
        .withSecond(0)
        .withNano(0)
        .format(DisplayDateTimeFormatter)

fun String.toApiUtcDateTimeOrNull(zoneId: ZoneId = ZoneId.systemDefault()): String? =
    parseToInstantOrNull(zoneId)?.toString()

fun String.toDisplayDateTimeOrSelf(zoneId: ZoneId = ZoneId.systemDefault()): String =
    parseToInstantOrNull(zoneId)
        ?.atZone(zoneId)
        ?.format(DisplayDateTimeFormatter)
        ?: trim()

fun String.toApiUtcStartOfDayOrNull(zoneId: ZoneId = ZoneId.systemDefault()): String? =
    parseToLocalDateOrNull(zoneId)
        ?.atStartOfDay(zoneId)
        ?.toInstant()
        ?.toString()

fun String.toDisplayDateOrSelf(zoneId: ZoneId = ZoneId.systemDefault()): String =
    parseToLocalDateOrNull(zoneId)
        ?.format(DisplayDateFormatter)
        ?: trim()

private fun String.parseToInstantOrNull(zoneId: ZoneId): Instant? {
    val value = trim()
    if (value.isBlank()) return null

    runCatching { Instant.parse(value) }.getOrNull()?.let { return it }
    runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()?.let { return it }
    runCatching { ZonedDateTime.parse(value).toInstant() }.getOrNull()?.let { return it }

    return LocalDateTimeParsers.firstNotNullOfOrNull { formatter ->
        runCatching {
            LocalDateTime.parse(value, formatter).atZone(zoneId).toInstant()
        }.getOrNull()
    }
}

private fun String.parseToLocalDateOrNull(zoneId: ZoneId): LocalDate? {
    val value = trim()
    if (value.isBlank()) return null

    runCatching { Instant.parse(value).atZone(zoneId).toLocalDate() }.getOrNull()?.let { return it }
    runCatching { OffsetDateTime.parse(value).toInstant().atZone(zoneId).toLocalDate() }.getOrNull()
        ?.let { return it }
    runCatching { ZonedDateTime.parse(value).toInstant().atZone(zoneId).toLocalDate() }.getOrNull()
        ?.let { return it }

    return LocalDateParsers.firstNotNullOfOrNull { formatter ->
        runCatching {
            LocalDate.parse(value, formatter)
        }.getOrNull()
    }
}
