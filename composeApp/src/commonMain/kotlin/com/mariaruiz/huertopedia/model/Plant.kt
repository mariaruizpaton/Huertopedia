package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: String = "",
    val nombreComun: LocalizedText = LocalizedText(),
    val nombreCientifico: String = "",
    val categoria: LocalizedText = LocalizedText(),
    val imagenUrl: String? = null,
    val siembra: LocalizedText = LocalizedText(),
    val recoleccion: LocalizedText = LocalizedText(),
    val temperaturaOptima: String = "",
    val riego: LocalizedText = LocalizedText(),
    val abono: LocalizedText = LocalizedText(),
    val cuidados: LocalizedText = LocalizedText(),
    val plantasAmigables: List<LocalizedText> = emptyList(),
    val plantasEnemigas: List<LocalizedText> = emptyList()
)
