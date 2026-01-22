package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

// Maceta
@Serializable
data class GardenFlowerpot(
    val id: String = "", // Formato "r{fila}_c{columna}"
    val planterId: String = "",
    val fila: Int = 0,
    val columna: Int = 0,
    val plantaId: String? = null,
    val nombrePlanta: String? = null,
    val imagenUrl: String? = null, // Para mostrar la foto en la cuadrícula
    val fechaSiembra: Long? = null,
    val tipoAccion: String? = null // "Plantar", "Sembrar" o "Recolectar"
)
