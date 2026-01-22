package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

// Planter
@Serializable
data class GardenPlant(
    val id: String = "",
    val nombrePersonal: String = "",
    val nombrePlantaOrigen: String = "",
    val fechaSiembra: Long = 0L,
    val estado: String = "Activo"
)