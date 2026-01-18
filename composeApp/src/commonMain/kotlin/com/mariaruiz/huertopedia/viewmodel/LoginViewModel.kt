package com.mariaruiz.huertopedia.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel {
    var name by mutableStateOf<String?>(null)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegisterMode by mutableStateOf(false)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    var errorMessage by mutableStateOf<String?>(null)

    var onRegisterRequested: ((String, String, String?, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLoginRequested: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLogoutRequested: (() -> Unit)? = null

    fun onAceptarClick() {
        errorMessage = null
        if (name?.isBlank() == true && isRegisterMode) {
            errorMessage = "El nombre no puede estar vacío"
            return
        }
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Rellena todos los campos"
            return
        }

        if (isRegisterMode) {
            println("Solicitando registro a Android...")
            onRegisterRequested?.invoke(email, password, name) { success, error ->
                if (!success) {
                    errorMessage = "Error en registro: $error"
                    println(errorMessage)
                }
            }
        } else {
            println("Solicitando login a Android...")
            onLoginRequested?.invoke(email, password) { success, error ->
                if (!success) {
                    errorMessage = "Error en login: $error"
                    println(errorMessage)
                }
            }
        }
    }

    fun logout() {
        onLogoutRequested?.invoke()
        email = ""
        password = ""
        errorMessage = null
        println("Sesión cerrada y datos limpiados")
    }
}
