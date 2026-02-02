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

/**
 * La actividad principal de la aplicación Android.
 *
 * Esta clase se encarga de:
 * - Configurar e inicializar los servicios de Firebase (Authentication y Firestore).
 * - Gestionar el flujo de inicio de sesión con Google, incluyendo la configuración de GSO
 *   y el manejo del resultado del lanzador de actividad.
 * - Inyectar la lógica específica de la plataforma (como los métodos de inicio de sesión,
 *   registro y cierre de sesión) en el [LoginViewModel] compartido.
 * - Renderizar el composable [App] principal, que constituye la UI de la aplicación.
 */
@Suppress("DEPRECATION")
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
                    onLoginResultCallback = callbackDeVuelta
                    launcher.launch(googleSignInClient.signInIntent)
                },
                onSetupViewModel = { viewModel ->
                    auth.addAuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser
                        viewModel.setLoggedIn(user != null)
                        if (user != null) {
                            viewModel.userId = user.uid

                            val userDocRef = db.collection("usuario").document(user.uid)
                            userDocRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    viewModel.name = document.getString("nombre")
                                    viewModel.descripcion = document.getString("descripcion") ?: ""
                                    viewModel.imagenUrl = document.getString("imagen_url") ?: ""
                                } else {
                                    val nameFromAuth = user.displayName
                                    viewModel.name = nameFromAuth
                                    
                                    val userData = hashMapOf(
                                        "id" to user.uid, 
                                        "nombre" to nameFromAuth,
                                        "email" to user.email,
                                        "descripcion" to "",
                                        "imagen_url" to "",
                                        "preferences" to hashMapOf(
                                            "language" to "es",
                                            "theme" to "system"
                                        )
                                    )
                                    userDocRef.set(userData)
                                }
                            }
                        } else {
                            viewModel.userId = ""
                            viewModel.name = null
                        }
                    }
                    setupLoginLogic(viewModel, auth, db, googleSignInClient)
                },
                onSetupWikiViewModel = {}
            )
        }
    }

    /**
     * Configura los callbacks del [LoginViewModel] para interactuar con Firebase y Google Sign-In.
     *
     * Esta función conecta las acciones del usuario en la UI (manejadas por el ViewModel)
     * con las implementaciones específicas de la plataforma Android para la autenticación.
     *
     * @param viewModel La instancia del [LoginViewModel] compartido.
     * @param auth La instancia de [FirebaseAuth].
     * @param db La instancia de [FirebaseFirestore].
     * @param googleClient El cliente de [GoogleSignIn] para gestionar el cierre de sesión de Google.
     */
    private fun setupLoginLogic(viewModel: LoginViewModel, auth: FirebaseAuth, db: FirebaseFirestore, googleClient: com.google.android.gms.auth.api.signin.GoogleSignInClient) {
        viewModel.onRegisterRequested = { email, password, nombre, onResult ->
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userData = hashMapOf(
                            "id" to uid,
                            "nombre" to nombre,
                            "email" to email,
                            "descripcion" to "",
                            "imagen_url" to "",
                            "preferences" to hashMapOf(
                                "language" to "es",
                                "theme" to "system"
                            )
                        )
                        db.collection("usuario").document(uid).set(userData)
                        viewModel.userId = uid
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
