package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: String = "",
    val nombreComun: String = "",
    val categoria: String = "",
    val imagenUrl: String? = null,
    val diasCosecha: Int = 0,
    val frecuenciaRiego: String = "",
    val plantasAmigables: List<String> = emptyList(),
    val plantasEnemigas: List<String> = emptyList(),
    val temporadaSiembra: List<String> = emptyList(),
    val tipoSustrato: String = ""
)
