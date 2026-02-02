package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

/**
 * Representa una planta con su información detallada.
 *
 * @property id El identificador único de la planta.
 * @property nombreComun El nombre común de la planta, en varios idiomas.
 * @property nombreCientifico El nombre científico de la planta.
 * @property categoria La categoría de la planta, en varios idiomas.
 * @property imagenUrl La URL de la imagen de la planta.
 * @property siembra Información sobre la siembra de la planta, en varios idiomas.
 * @property recoleccion Información sobre la recolección de la planta, en varios idiomas.
 * @property temperaturaOptima La temperatura óptima para el cultivo de la planta.
 * @property riego Información sobre el riego de la planta, en varios idiomas.
 * @property abono Información sobre el abono de la planta, en varios idiomas.
 * @property cuidados Cuidados generales de la planta, en varios idiomas.
 * @property plantasAmigables Lista de plantas que se benefician de estar cerca de esta planta.
 * @property plantasEnemigas Lista de plantas que se ven perjudicadas por estar cerca de esta planta.
 */
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
