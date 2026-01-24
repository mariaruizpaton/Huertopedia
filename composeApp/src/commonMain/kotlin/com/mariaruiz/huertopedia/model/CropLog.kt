package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

@Serializable
data class CropLog(
    val id: String = "",
    val planterId: String,
    val timestamp: Long,
    val eventType: String,
    val notes: String? = null,
    val irrigationType: String? = null,
    val irrigationMinutes: Int? = null,
    val photoPath: String? = null
)
