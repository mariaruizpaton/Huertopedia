package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

/**
 * Representa una maceta individual dentro de una jardinera en el huerto.
 *
 * @property id El identificador único de la maceta.
 * @property planterId El identificador de la jardinera a la que pertenece esta maceta.
 * @property fila La fila en la que se encuentra la maceta dentro de la jardinera.
 * @property columna La columna en la que se encuentra la maceta dentro de la jardinera.
 * @property plantaId El identificador de la planta que está en esta maceta (si la hay).
 * @property nombrePlanta El nombre localizado de la planta en esta maceta.
 * @property imagenUrl La URL de la imagen de la planta en esta maceta.
 * @property fechaSiembra La fecha en que se sembró la planta.
 * @property tipoAccion El último tipo de acción realizada en esta maceta.
 */
@Serializable
data class GardenFlowerpot(
    val id: String = "", 
    val planterId: String = "",
    val fila: Int = 0,
    val columna: Int = 0,
    val plantaId: String? = null,
    val nombrePlanta: LocalizedText? = null,
    val imagenUrl: String? = null, 
    val fechaSiembra: Long? = null,
    val tipoAccion: LocalizedText? = null // CORREGIDO
)
