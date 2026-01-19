package com.mariaruiz.huertopedia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webClientId = "21402074340-r9cne0sdceh44qjsotpjtj3achl5f05m.apps.googleusercontent.com"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        setContent {
            var onLoginResultCallback: ((Boolean) -> Unit)? = null

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { authTask ->
                            if (!authTask.isSuccessful) {
                                Log.e("FirebaseLogin", "Error", authTask.exception)
                            }
                            onLoginResultCallback?.invoke(authTask.isSuccessful)
                        }
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "Error code: ${e.statusCode}")
                    onLoginResultCallback?.invoke(false)
                }
            }

            App(
                onGoogleLogin = { callbackDeVuelta ->
                    Log.d("DEBUG_APP", "Lanzando ventana de Google...")
                    onLoginResultCallback = callbackDeVuelta
                    launcher.launch(googleSignInClient.signInIntent)
                },
                onSetupViewModel = { viewModel ->
                    auth.addAuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser
                        viewModel.setLoggedIn(user != null)

                        if (user != null) {
                            // --- CAMBIO CLAVE: Guardar el ID para evitar el error de segmentos ---
                            viewModel.userId = user.uid

                            val userDocRef = db.collection("usuario").document(user.uid)
                            userDocRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    viewModel.name = document.getString("nombre")
                                    // --- NUEVO: Cargar descripción e imagen para que salgan en el perfil ---
                                    viewModel.descripcion = document.getString("descripcion") ?: ""
                                    viewModel.imagenUrl = document.getString("imagen_url") ?: ""

                                    Log.d("AuthState", "Datos cargados: ${viewModel.name}, ${viewModel.descripcion}")
                                } else {
                                    // Registro de nuevo usuario (Google)
                                    val nameFromAuth = user.displayName
                                    viewModel.name = nameFromAuth
                                    viewModel.descripcion = ""
                                    viewModel.imagenUrl = ""

                                    val userData = hashMapOf(
                                        "nombre" to nameFromAuth,
                                        "email" to user.email,
                                        "descripcion" to "",
                                        "imagen_url" to ""
                                    )
                                    userDocRef.set(userData)
                                }
                            }
                        } else {
                            viewModel.userId = "" // Limpiar ID al cerrar sesión
                            viewModel.name = null
                        }
                    }
                    setupViewModelLogic(viewModel, auth, db, googleSignInClient)
                }
            )
        }
    }

    private fun setupViewModelLogic(viewModel: LoginViewModel, auth: FirebaseAuth, db: FirebaseFirestore, googleClient: com.google.android.gms.auth.api.signin.GoogleSignInClient) {
        viewModel.onRegisterRequested = { email, password, nombre, onResult ->
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Inicializamos el documento con todos los campos
                        val userData = hashMapOf(
                            "nombre" to nombre,
                            "email" to email,
                            "descripcion" to "",
                            "imagen_url" to ""
                        )
                        db.collection("usuario").document(uid).set(userData)
                        viewModel.userId = uid // IMPORTANTE
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
        }

        viewModel.onLoginRequested = { email, password, onResult ->
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                onResult(task.isSuccessful, task.exception?.message)
            }
        }

        viewModel.onLogoutRequested = {
            auth.signOut()
            googleClient.signOut()
        }
    }
}