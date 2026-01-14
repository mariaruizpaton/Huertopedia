package com.mariaruiz.huertopedia

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview

import com.mariaruiz.huertopedia.screens.HomeScreen
import com.mariaruiz.huertopedia.screens.LoginScreen
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel


// ... (El colorScheme y el enum FormState se mantienen igual)
private val GardenColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // Verde para botones y elementos principales
    background = Color(0xFFF0F4E8), // Fondo verde claro, como un campo fresco
    surface = Color.White, // Blanco para las tarjetas y superficies
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
@Preview
fun LogIn(
    // Pasamos una función que recibe un "callback" (onResult)
    // Cuando Android termine el login, llamará a 'onResult(true)' si fue bien.
    onGoogleLogin: (onResult: (Boolean) -> Unit) -> Unit = { _ -> },
    // NUEVO PARÁMETRO: Una función que nos deja configurar el ViewModel desde fuera
    onSetupViewModel: (LoginViewModel) -> Unit = {}
) {
    MaterialTheme(colorScheme = GardenColorScheme) {
        val viewModel = remember { LoginViewModel() }

        // 2. IMPORTANTE: Llamamos a la configuración nada más crear el ViewModel
        // Esto conecta los cables del Paso 2 con el Paso 1
        LaunchedEffect(Unit) {
            onSetupViewModel(viewModel)
        }

        if (viewModel.isLoggedIn) {
            HomeScreen(onLogout = { viewModel.logout() }, viewModel = viewModel)
        } else {
            LoginScreen(
                viewModel = viewModel,
                onGoogleLoginRequest = {
                    // Llamamos a la parte nativa y le decimos:
                    // "Cuando termines, ejecuta este bloque"
                    onGoogleLogin { success ->
                        if (success) {
                            // Solo si el login fue real y exitoso, entramos
                            viewModel.onGoogleLogin() // Esto pone isLoggedIn = true
                        } else {
                            println("El usuario canceló o falló el login")
                        }
                    }
                }
            )
        }
    }
}
