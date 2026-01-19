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
        if (name?.isBlank() == true && isRegisterMode) {
            errorMessage = "El nombre no puede estar vacío"
            return
        }
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Rellena todos los campos"
            return
        }

        if (isRegisterMode) {
            onRegisterRequested?.invoke(email, password, name) { success, uid ->
                if (success && uid != null) {
                    this.userId = uid // Guardamos el ID del documento
                    setLoggedIn(true)
                } else {
                    errorMessage = "Error en registro: $uid"
                }
            }
        } else {
            onLoginRequested?.invoke(email, password) { success, uid ->
                if (success && uid != null) {
                    this.userId = uid // Guardamos el ID del documento
                    setLoggedIn(true)
                } else {
                    errorMessage = "Error en login: $uid"
                }
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
