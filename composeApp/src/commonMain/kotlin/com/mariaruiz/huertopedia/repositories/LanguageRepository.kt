/**
 * Repositorio encargado de gestionar las preferencias de idioma de la aplicación.
 * Permite guardar y obtener el idioma tanto localmente como en Firestore si el usuario está logueado.
 */
package com.mariaruiz.huertopedia.repositories

import com.russhwolf.settings.Settings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LanguageRepository {
    private val settings: Settings = Settings()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _currentLanguage = MutableStateFlow<String>(settings.getString("pref_lang", "es"))
    /**
     * Flujo que emite el código de idioma actual (por ejemplo, "es" para español, "en" para inglés).
     */
    val currentLanguage: StateFlow<String> = _currentLanguage

    /**
     * Establece el idioma de la aplicación y lo sincroniza con Firestore si hay un usuario logueado.
     *
     * @param langCode El código del idioma a establecer.
     */
    suspend fun setLanguage(langCode: String) {
        // 1. Guardar en Local
        settings.putString("pref_lang", langCode)
        _currentLanguage.value = langCode

        // 2. Sincronizar con Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val dataToSave = UserPreferencesWrapper(
                    preferences = LanguagePreference(language = langCode)
                )

                // CORRECCIÓN AQUÍ:
                // Añadimos <UserPreferencesWrapper> explícitamente
                // para decirle al compilador cuál es el tipo 'T'.
                db.collection("usuario")
                    .document(uid)
                    .set<UserPreferencesWrapper>(dataToSave, merge = true)

            } catch (e: Exception) {
                println("Error sincronizando idioma en Firebase: ${e.message}")
            }
        }
    }
}

/**
 * Clase de datos para envolver las preferencias de idioma del usuario para la serialización de Firestore.
 */
@Serializable
data class UserPreferencesWrapper(
    val preferences: LanguagePreference
)

/**
 * Clase de datos que representa la preferencia de idioma del usuario.
 */
@Serializable
data class LanguagePreference(
    val language: String
)