package com.mariaruiz.huertopedia.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LoginViewModel {
    // --- ESTADO (Variables) ---
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Controla si mostramos el formulario de Registro o de Iniciar Sesión
    var isRegisterMode by mutableStateOf(false)

    // Controla si el usuario ya ha entrado correctamente
    var isLoggedIn by mutableStateOf(false)

    // Variable para mostrar errores en pantalla si algo falla
    var errorMessage by mutableStateOf<String?>(null)

    // Estas variables se rellenarán desde fuera (MainActivity)

    // Función para registrar: (email, password, nombre) -> callback(exito, error)
    var onRegisterRequested: ((String, String, String, (Boolean, String?) -> Unit) -> Unit)? = null

    // Función para login: (email, password) -> callback(exito, error)
    var onLoginRequested: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null

    // Es una función que no recibe nada y no devuelve nada
    var onLogoutRequested: (() -> Unit)? = null


    // --- LÓGICA (Funciones) ---

    fun onAceptarClick() {
        errorMessage = null // Limpiamos errores previos

        if (name.isBlank() && isRegisterMode) {
            errorMessage = "El nombre no puede estar vacío"
            return
        }
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Rellena todos los campos"
            return
        }

        if (isRegisterMode) {
            println("Solicitando registro a Android...")
            // Llamamos a la función "hueca" que Android rellenará
            onRegisterRequested?.invoke(email, password, name) { success, error ->
                if (success) {
                    isLoggedIn = true
                } else {
                    errorMessage = "Error en registro: $error"
                    println(errorMessage)
                }
            }
        } else {
            println("Solicitando login a Android...")
            // Llamamos a la función de login
            onLoginRequested?.invoke(email, password) { success, error ->
                if (success) {
                    isLoggedIn = true
                } else {
                    errorMessage = "Error en login: $error"
                    println(errorMessage)
                }
            }
        }
    }

    // --- MODIFICA TU FUNCIÓN LOGOUT ---
    fun logout() {
        // Ejecutamos la lógica de Firebase que definimos en MainActivity
        onLogoutRequested?.invoke()

        // Limpiamos el estado visual
        isLoggedIn = false
        name = ""
        email = ""
        password = ""
        errorMessage = null

        println("Sesión cerrada y datos limpiados")
    }

    fun onGoogleLogin() {
        // Aquí solo actualizamos el estado, la lógica nativa se llama desde fuera
        isLoggedIn = true
    }

    // En LoginViewModel.kt
    fun checkUserSession(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            isLoggedIn = true
        }
    }

}
