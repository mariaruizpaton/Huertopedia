package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

/**
 * Representa un texto que puede ser localizado en diferentes idiomas.
 *
 * @property es El texto en español.
 * @property en El texto en inglés.
 */
@Serializable
data class LocalizedText(
    val es: String = "",
    val en: String = ""
) {
    /**
     * Devuelve el texto según el código de idioma ("es", "en")
     */
    fun get(langCode: String): String {
        return when (langCode.lowercase()) {
            "es" -> es
            else -> en
        }
    }
}
