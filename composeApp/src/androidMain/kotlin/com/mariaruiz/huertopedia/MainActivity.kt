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
                            // El AuthStateListener se encargará de actualizar el estado de la UI.
                            // Esta llamada solo notifica al composable que la operación ha terminado.
                            onLoginResultCallback?.invoke(authTask.isSuccessful)
                        }
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "Error code: ${e.statusCode}")
                    onLoginResultCallback?.invoke(false)
                }
            }

            LogIn(
                onGoogleLogin = { callbackDeVuelta ->
                    Log.d("DEBUG_APP", "Lanzando ventana de Google...")
                    onLoginResultCallback = callbackDeVuelta
                    launcher.launch(googleSignInClient.signInIntent)
                },
                onSetupViewModel = { viewModel ->
                    // El AuthStateListener es la fuente de verdad para el estado de autenticación.
                    auth.addAuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            // Usuario ha iniciado sesión
                            viewModel.isLoggedIn = true

                            val userDocRef = db.collection("usuario").document(user.uid)
                            userDocRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // El usuario ya existe en Firestore, obtenemos su nombre.
                                    viewModel.name = document.getString("nombre")
                                    Log.d("AuthState", "Usuario encontrado en Firestore. Nombre: ${viewModel.name}")
                                } else {
                                    // El usuario no está en Firestore (nuevo con Google).
                                    // Usamos su nombre de Google y lo guardamos.
                                    val nameFromAuth = user.displayName
                                    viewModel.name = nameFromAuth
                                    Log.d("AuthState", "Nuevo usuario de Google. Nombre: $nameFromAuth")

                                    val userData = hashMapOf("nombre" to nameFromAuth, "email" to user.email)
                                    userDocRef.set(userData).addOnFailureListener { e ->
                                        Log.e("AuthState", "Error al guardar nuevo usuario en Firestore", e)
                                    }
                                }
                            }.addOnFailureListener { e ->
                                // Si falla la lectura de Firestore, usamos el nombre de Google como fallback.
                                Log.e("AuthState", "Error al leer Firestore, fallback a displayName", e)
                                viewModel.name = user.displayName
                            }
                        } else {
                            // Usuario ha cerrado sesión
                            viewModel.isLoggedIn = false
                            viewModel.name = null
                            Log.d("AuthState", "Usuario ha cerrado sesión.")
                        }
                    }
                    // Configura las acciones que la UI puede solicitar.
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
                    val userData = hashMapOf("nombre" to nombre, "email" to email)
                    if (uid != null) db.collection("usuario").document(uid).set(userData)
                    // El AuthStateListener se encargará de actualizar el estado.
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
        }

        viewModel.onLoginRequested = { email, password, onResult ->
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                // El AuthStateListener se encargará de actualizar el estado.
                // Solo notificamos el resultado de la operación.
                onResult(task.isSuccessful, task.exception?.message)
            }
        }

        viewModel.onLogoutRequested = {
            auth.signOut()
            googleClient.signOut()
            // El AuthStateListener se encargará de actualizar el estado.
        }
    }
}