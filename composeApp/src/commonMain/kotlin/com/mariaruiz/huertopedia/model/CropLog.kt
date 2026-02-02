package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

/**
 * Representa una entrada en el registro de un cultivo.
 *
 * @property id El identificador único de la entrada del registro.
 * @property planterId El identificador de la maceta asociada a este registro.
 * @property userId El identificador del usuario que creó este registro.
 * @property timestamp La fecha y hora en que se creó el registro.
 * @property eventType El tipo de evento que se está registrando (por ejemplo, riego, siembra, etc.).
 * @property notes Notas adicionales sobre el evento.
 * @property irrigationType El tipo de riego, si el evento es de riego.
 * @property irrigationMinutes La duración en minutos del riego, si el evento es de riego.
 * @property photoPath La ruta a una foto asociada con el evento.
 */
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
