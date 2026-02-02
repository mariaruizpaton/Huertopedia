/**
 * Este archivo contiene funciones de utilidad para trabajar con fechas y horas.
 */
package com.mariaruiz.huertopedia.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Convierte un timestamp en milisegundos a una cadena de fecha legible por humanos en formato "dd/MM/yyyy".
 *
 * @return La fecha formateada como String, o "Sin fecha" si el timestamp es 0.
 */
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

/**
 * Convierte un timestamp en milisegundos a una cadena de fecha y hora legible por humanos en formato "dd/MM/yyyy HH:mm".
 *
 * @return La fecha y hora formateadas como String, o "Sin fecha" si el timestamp es 0.
 */
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