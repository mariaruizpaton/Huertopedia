package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

// Diario de Cultivo
@Serializable
data class CropLog(
    val id: String = "", // CropLog-NombreJardinera
    val fecha: Long = 0L,
    val nota: String = "",
    val urlFoto: String? = null,
    val tipoEvento: String = "Observación" // Ej: Riego, Abono, Cosecha...
)