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
                errorMessage = "error_unknown"
            }
        }
    }

    var errorMessage by mutableStateOf<String?>(null)

    var onRegisterRequested: ((String, String, String?, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLoginRequested: ((String, String, (Boolean, String?) -> Unit) -> Unit)? = null
    var onLogoutRequested: (() -> Unit)? = null

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
                    // El ID se guardará mediante el AuthStateListener en MainActivity
                    setLoggedIn(true)
                } else {
                    errorMessage = translateFirebaseError(result ?: "")
                }
            }
        } else {
            onLoginRequested?.invoke(email, password) { success, result ->
                if (success) {
                    // CAMBIO IMPORTANTE: No tocamos userId aquí para no sobreescribirlo con ""
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
            message.contains("credential") || message.contains("password") || message.contains("auth") || message.contains("incorrect") -> "error_invalid_credentials"
            message.contains("user-not-found") || message.contains("no user") -> "error_user_not_found"
            message.contains("email") && (message.contains("already") || message.contains("exists") || message.contains("use") || message.contains("registrado")) -> "error_email_already_in_use"
            message.contains("weak-password") || message.contains("short") -> "error_weak_password"
            message.contains("invalid-email") || message.contains("malformed") -> "error_invalid_email"
            message.contains("network") || message.contains("timeout") || message.contains("connection") -> "error_network"
            else -> "error_unknown"
        }
    }

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

    var descripcion by mutableStateOf("")
    var imagenUrl by mutableStateOf("")
    var userId by mutableStateOf("")

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

    var imagenUrlRenderizable by mutableStateOf<String?>(null)

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
