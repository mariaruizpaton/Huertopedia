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

/**
 * ViewModel para gestionar la lógica de autenticación y el perfil de usuario.
 *
 * Maneja el estado para el registro, inicio de sesión, cierre de sesión y la actualización
 * de los datos del usuario (nombre, descripción, imagen de perfil).
 */
class LoginViewModel : ViewModel() {
    // --- Propiedades para el formulario de Login/Registro ---
    var name by mutableStateOf<String?>(null)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegisterMode by mutableStateOf(false)

    // --- Estado de autenticación ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    /**
     * Actualiza el estado de autenticación.
     * @param isLoggedIn `true` si el usuario ha iniciado sesión, `false` en caso contrario.
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    /**
     * Obtiene los datos del usuario actual desde Firestore y actualiza el estado del ViewModel.
     */
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
                errorMessage = "error_unknown"
            }
        }
    }

    // --- Gestión de errores y acciones ---
    var errorMessage by mutableStateOf<String?>(null)

    // Callbacks para delegar la lógica de Firebase a la capa de plataforma (Android/iOS)
    var onRegisterRequested: ((String, String, String?, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLoginRequested: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLogoutRequested: (() -> Unit)? = null

    /**
     * Gestiona el clic en el botón "Aceptar" del formulario.
     * Valida los campos y llama a la acción de registro o inicio de sesión correspondiente.
     */
    fun onAceptarClick() {
        errorMessage = null
        
        if (isRegisterMode && (name == null || name?.isBlank() == true)) {
            errorMessage = "error_name_empty"
            return
        }
        
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "error_fields_empty"
            return
        }

        if (isRegisterMode) {
            onRegisterRequested?.invoke(email, password, name) { success, result ->
                if (success) {
                    setLoggedIn(true)
                } else {
                    errorMessage = translateFirebaseError(result ?: "")
                }
            }
        } else {
            onLoginRequested?.invoke(email, password) { success, result ->
                if (success) {
                    setLoggedIn(true)
                } else {
                    errorMessage = translateFirebaseError(result ?: "")
                }
            }
        }
    }

    /**
     * Traduce los mensajes de error de Firebase a claves de cadenas de recursos para internacionalización.
     * @param firebaseMessage El mensaje de error original de Firebase.
     * @return Una clave de recurso de cadena que representa el error.
     */
    private fun translateFirebaseError(firebaseMessage: String): String {
        val message = firebaseMessage.lowercase()
        return when {
            message.contains("credential") || message.contains("password") || message.contains("auth") || message.contains("incorrect") -> "error_invalid_credentials"
            message.contains("user-not-found") || message.contains("no user") -> "error_user_not_found"
            message.contains("email") && (message.contains("already") || message.contains("exists") || message.contains("use") || message.contains("registrado")) -> "error_email_already_in_use"
            message.contains("weak-password") || message.contains("short") -> "error_weak_password"
            message.contains("invalid-email") || message.contains("malformed") -> "error_invalid_email"
            message.contains("network") || message.contains("timeout") || message.contains("connection") -> "error_network"
            else -> "error_unknown"
        }
    }

    /**
     * Cierra la sesión del usuario y limpia todos los datos relacionados.
     */
    fun logout() {
        onLogoutRequested?.invoke()
        email = ""
        password = ""
        userId = ""
        name = null
        descripcion = ""
        imagenUrl = ""
        imagenUrlRenderizable = null
        errorMessage = null
    }

    // --- Propiedades del perfil de usuario ---
    var descripcion by mutableStateOf("")
    var imagenUrl by mutableStateOf("") // Path en Storage
    var userId by mutableStateOf("")

    /**
     * Sube una imagen de perfil (`ByteArray`) a Firebase Storage.
     * @param bytes Los datos de la imagen.
     */
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
                errorMessage = "error_unknown"
            }
        }
    }

    /**
     * Actualiza los datos del usuario (nombre, descripción, URL de imagen) en Firestore.
     * @param newName El nuevo nombre.
     * @param newDesc La nueva descripción.
     * @param newImageUrl La nueva ruta de la imagen en Storage.
     */
    fun updateUserData(newName: String, newDesc: String, newImageUrl: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val userDocument = db.collection("usuario").document(userId)
                userDocument.update("nombre" to newName, "descripcion" to newDesc, "imagen_url" to newImageUrl)
                name = newName
                descripcion = newDesc
                imagenUrl = newImageUrl
            } catch (e: Exception) {
                errorMessage = "error_unknown"
            }
        }
    }

    // URL pública de la imagen para mostrar en la UI
    var imagenUrlRenderizable by mutableStateOf<String?>(null)

    /**
     * Convierte la ruta de Storage de la imagen de perfil a una URL de descarga pública.
     */
    fun obtenerUrlDescarga() {
        val path = imagenUrl
        if (path.isBlank()) {
            imagenUrlRenderizable = null
            return
        }
        viewModelScope.launch {
            try {
                val downloadUrl = Firebase.storage.reference.child(path).getDownloadUrl()
                imagenUrlRenderizable = downloadUrl
            } catch (e: Exception) {
                imagenUrlRenderizable = null
            }
        }
    }
}
