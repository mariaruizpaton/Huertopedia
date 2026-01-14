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

        // 1. CONFIGURACIÓN (Fuera del setContent para estabilidad)
        val webClientId = "21402074340-r9cne0sdceh44qjsotpjtj3achl5f05m.apps.googleusercontent.com"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        setContent {
            // Guardamos el callback en un estado para que no se pierda al recomponer
            var onLoginResultCallback: ((Boolean) -> Unit)? = null

            // 2. EL LAUNCHER (Debe estar dentro de setContent o como propiedad de la clase)
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener(this) { authTask ->
                                if (authTask.isSuccessful) {
                                    Log.d("FirebaseLogin", "OK")
                                    onLoginResultCallback?.invoke(true)
                                } else {
                                    Log.e("FirebaseLogin", "Error", authTask.exception)
                                    onLoginResultCallback?.invoke(false)
                                }
                            }
                    }
                } catch (e: ApiException) {
                    // SI AQUÍ SALE ERROR 10 o 12500, es por el SHA-1 en Firebase
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
                    val currentUser = auth.currentUser

                    if (currentUser != null) {
                        viewModel.isLoggedIn = true

                        // --- NUEVO CÓDIGO: OBTENER EL NOMBRE ---

                        // CASO 1: Intentar obtener el nombre directo de la cuenta (Google suele tenerlo)
                        val nameFromAuth = currentUser.displayName

                        if (!nameFromAuth.isNullOrEmpty()) {
                            viewModel.name = nameFromAuth
                            Log.d("UserCheck", "Nombre obtenido de Auth: $nameFromAuth")
                        } else {
                            // CASO 2: Si es null (común en email/pass), lo buscamos en tu Firestore
                            db.collection("usuario").document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val nameFromDb = document.getString("nombre")
                                        viewModel.name = nameFromDb
                                        Log.d("UserCheck", "Nombre obtenido de Firestore: $nameFromDb")
                                    }
                                }
                        }
                    }

                    // Configuración estándar
                    setupViewModelLogic(viewModel, auth, db, googleSignInClient)
                }
            )
        }
    }

    // Función auxiliar para no ensuciar el onCreate
    private fun setupViewModelLogic(viewModel: LoginViewModel, auth: FirebaseAuth, db: FirebaseFirestore, googleClient: com.google.android.gms.auth.api.signin.GoogleSignInClient) {
        viewModel.onRegisterRequested = { email, password, nombre, onResult ->
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val userData = hashMapOf("nombre" to nombre, "email" to email)
                    if (uid != null) db.collection("usuario").document(uid).set(userData)
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
