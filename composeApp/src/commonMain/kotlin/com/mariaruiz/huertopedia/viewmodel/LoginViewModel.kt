package com.mariaruiz.huertopedia.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.utils.toFirebaseData
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.Data

class LoginViewModel : ViewModel() {
    var name by mutableStateOf<String?>(null)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegisterMode by mutableStateOf(false)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    fun fetchUserData() {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                val document = Firebase.firestore.collection("usuario").document(userId).get()
                if (document.exists) {
                    name = document.get<String>("nombre")
                    descripcion = document.get<String>("descripcion") ?: ""
                    imagenUrl = document.get<String>("imagen_url") ?: ""
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar datos: ${e.message}"
            }
        }
    }

    var errorMessage by mutableStateOf<String?>(null)

    var onRegisterRequested: ((String, String, String?, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLoginRequested: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLogoutRequested: (() -> Unit)? = null

    fun onAceptarClick() {
        errorMessage = null
        // ... validaciones de campos vacíos ...

        if (isRegisterMode) {
            onRegisterRequested?.invoke(email, password, name) { success, result ->
                if (success) {
                    this.userId = result ?: ""
                    setLoggedIn(true)
                } else {
                    errorMessage = translateFirebaseError(result ?: "")
                }
            }
        } else {
            onLoginRequested?.invoke(email, password) { success, result ->
                if (success) {
                    this.userId = result ?: ""
                    setLoggedIn(true)
                } else {
                    errorMessage = translateFirebaseError(result ?: "")
                }
            }
        }
    }

    private fun translateFirebaseError(firebaseMessage: String): String {
        val message = firebaseMessage.lowercase()
        return when {
            // Credenciales incorrectas
            message.contains("credential") ||
                    message.contains("password") ||
                    message.contains("auth") ||
                    message.contains("incorrect") -> "error_invalid_credentials"

            // Usuario no encontrado
            message.contains("user-not-found") ||
                    message.contains("no user") -> "error_user_not_found"

            // EMAIL YA EXISTE (Mejorado para capturar todas las variantes)
            message.contains("email") && (
                    message.contains("already") ||
                            message.contains("exists") ||
                            message.contains("use") ||
                            message.contains("registrado")
                    ) -> "error_email_already_in_use"

            // Contraseña débil
            message.contains("weak-password") ||
                    message.contains("short") -> "error_weak_password"

            // Email mal formado
            message.contains("invalid-email") ||
                    message.contains("malformed") -> "error_invalid_email"

            // Red
            message.contains("network") ||
                    message.contains("timeout") ||
                    message.contains("connection") -> "error_network"

            else -> {
                println("DEBUG_FIREBASE_ERROR: $firebaseMessage")
                "error_unknown"
            }
        }
    }

    fun logout() {
        onLogoutRequested?.invoke()

        // 1. Limpiamos credenciales
        email = ""
        password = ""
        userId = ""

        // 2. Limpiamos datos de perfil
        name = null
        descripcion = ""

        // 3. ¡ESTO ES LO MÁS IMPORTANTE!
        // Limpiamos las URLs para que no se vea la foto anterior
        imagenUrl = ""
        imagenUrlRenderizable = null

        // 4. Limpiamos listas si las añadiste
        // favoritasIds = emptyList()
        // wishlistIds = emptyList()

        errorMessage = null
        println("Sesión cerrada y datos limpiados por completo")
    }

    fun onGoogleLogin() {
        // La lógica de login está en MainActivity y el estado se actualiza con el AuthStateListener.
    }

    var descripcion by mutableStateOf("")
    var imagenUrl by mutableStateOf("")
    var userId by mutableStateOf("") // Necesitas el ID del documento para actualizar

// ... dentro del ViewModel ...

    fun uploadImageBytes(bytes: ByteArray) {
        if (userId.isBlank()) return

        viewModelScope.launch {
            try {
                val pathRelativa = "icons/$userId.jpg"
                val storageRef = Firebase.storage.reference.child(pathRelativa)
                val dataToUpload = bytes.toFirebaseData()
                storageRef.putData(dataToUpload)
                updateUserData(name ?: "", descripcion, pathRelativa)

            } catch (e: Exception) {
                errorMessage = "Error en Storage: ${e.message}"
            }
        }
    }

    fun updateUserData(newName: String, newDesc: String, newImageUrl: String) {
        if (userId.isBlank()) {
            errorMessage = "ID de usuario no encontrado"
            return
        }

        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val userDocument = db.collection("usuario").document(userId)

                userDocument.update(
                    "nombre" to newName,
                    "descripcion" to newDesc,
                    "imagen_url" to newImageUrl
                )

                // Actualizar estado local para que la UI se refresque
                name = newName
                descripcion = newDesc
                imagenUrl = newImageUrl
            } catch (e: Exception) {
                errorMessage = "Error en Firestore: ${e.message}"
            }
        }
    }

    var imagenUrlRenderizable by mutableStateOf<String?>(null)

    fun obtenerUrlDescarga() {
        val path = imagenUrl // Aquí está el "icons/nombre.jpg"
        if (path.isBlank()) {
            imagenUrlRenderizable = null
            return
        }

        viewModelScope.launch {
            try {
                // Hacemos lo mismo que en WikiViewModel: convertir ruta corta a URL larga
                val downloadUrl = Firebase.storage.reference.child(path).getDownloadUrl()
                imagenUrlRenderizable = downloadUrl
            } catch (e: Exception) {
                println("Error al obtener URL del perfil: ${e.message}")
                imagenUrlRenderizable = null
            }
        }
    }
}
