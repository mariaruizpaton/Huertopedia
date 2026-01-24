package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: String = "",
    val nombreComun: String = "",
    val nombreCientifico: String = "",
    val categoria: String = "",
    val imagenUrl: String? = null,
    val siembra: String = "",
    val recoleccion: String = "",
    val temperaturaOptima: String = "",
    val riego: String = "",
    val abono: String = "",
    val cuidados: String = "",
    val plantasAmigables: List<String> = emptyList(),
    val plantasEnemigas: List<String> = emptyList()
)
