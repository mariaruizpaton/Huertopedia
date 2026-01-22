package com.mariaruiz.huertopedia.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

// Función de extensión para convertir Long a "dd/MM/yyyy"
@OptIn(ExperimentalTime::class)
fun Long.toHumanDateString(): String {
    if (this == 0L) return "Sin fecha"

    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year

    return "$day/$month/$year"
}