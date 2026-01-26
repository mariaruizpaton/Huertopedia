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

// NUEVA: Función para "dd/MM/yyyy HH:mm" (Ideal para logs y última actividad)
@OptIn(ExperimentalTime::class)
fun Long.toHumanDateTimeString(): String {
    if (this == 0L) return "Sin fecha"

    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year

    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')

    return "$day/$month/$year $hour:$minute"
}