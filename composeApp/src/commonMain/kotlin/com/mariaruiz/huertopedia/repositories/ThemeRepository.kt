/**
 * Repositorio encargado de gestionar las preferencias de tema de la aplicación.
 * Permite guardar y obtener el tema tanto localmente como en Firestore si el usuario está logueado.
 */
package com.mariaruiz.huertopedia.repositories

import com.russhwolf.settings.Settings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeRepository {
    private val settings: Settings = com.russhwolf.settings.Settings()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // "system" (por defecto), "light", "dark"
    private val _themePreference = MutableStateFlow<String>(settings.getString("pref_theme", "system"))
    /**
     * Flujo que emite la preferencia de tema actual. Puede ser "system", "light" o "dark".
     */
    val themePreference: StateFlow<String> = _themePreference

    /**
     * Establece el tema de la aplicación y lo sincroniza con Firestore si hay un usuario logueado.
     *
     * @param theme El tema a establecer ("system", "light" o "dark").
     */
    suspend fun setTheme(theme: String) {
        // 1. Guardar en Local (Settings)
        settings.putString("pref_theme", theme)
        _themePreference.value = theme

        // 2. Sincronizar con Firestore si hay usuario logueado
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val update = mapOf(
                    "preferences" to mapOf(
                        "theme" to theme
                    )
                )
                db.collection("usuario").document(uid).set(update, merge = true)
            } catch (e: Exception) {
                println("Error syncing theme: ${e.message}")
            }
        }
    }
}
