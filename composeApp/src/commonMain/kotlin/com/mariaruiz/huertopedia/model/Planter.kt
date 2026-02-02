package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

/**
 * Representa una jardinera o macetero en el huerto.
 *
 * @property id El identificador único de la jardinera.
 * @property nombre El nombre asignado a la jardinera.
 * @property filas El número de filas en la jardinera.
 * @property columnas El número de columnas en la jardinera.
 * @property fechaCreacion La fecha y hora en que se creó la jardinera.
 */
@Serializable
data class Planter(
    val id: String = "",
    val nombre: String = "",
    val filas: Int = 1,
    val columnas: Int = 1,
    val fechaCreacion: Long = 0L
)