package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class CropLog(
    val id: String = "",
    val planterId: String,
    val userId: String = "",
    val timestamp: Long,
    val eventType: LocalizedText,
    val notes: LocalizedText? = null, // <--- CAMBIADO A LocalizedText
    val irrigationType: LocalizedText? = null,
    val irrigationMinutes: Int? = null,
    val photoPath: String? = null
)
