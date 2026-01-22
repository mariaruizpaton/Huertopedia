package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

// jardinera
@Serializable
data class Planter(
    val id: String = "",
    val nombre: String = "",
    val filas: Int = 1,
    val columnas: Int = 1,
    val fechaCreacion: Long = 0L
)