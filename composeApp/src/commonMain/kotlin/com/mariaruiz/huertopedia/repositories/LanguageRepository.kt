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
    val currentLanguage: StateFlow<String> = _currentLanguage

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

@Serializable
data class UserPreferencesWrapper(
    val preferences: LanguagePreference
)

@Serializable
data class LanguagePreference(
    val language: String
)