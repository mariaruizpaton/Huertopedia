package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: String = "",
    val nombre_comun: String = "",
    val categoria: String = "",
    val imagen_url: String? = null,
    val dias_cosecha: Int = 0,
    val frecuencia_riego: String = "",
    val plantas_amigables: List<String> = emptyList(),
    val plantas_enemigas: List<String> = emptyList(),
    val temporada_siembra: List<String> = emptyList(),
    val tipo_sustrato: String = ""
)
