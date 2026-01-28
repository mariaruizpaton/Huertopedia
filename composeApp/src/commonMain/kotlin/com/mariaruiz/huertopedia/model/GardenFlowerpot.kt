package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

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
    val tipoAccion: String? = null 
)
