package com.mariaruiz.huertopedia.model

import kotlinx.serialization.Serializable

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
