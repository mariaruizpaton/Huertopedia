package com.mariaruiz.huertopedia

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.mariaruiz.huertopedia.screens.GardenScreen
import com.mariaruiz.huertopedia.screens.HomeScreen
import com.mariaruiz.huertopedia.screens.LoginScreen
import com.mariaruiz.huertopedia.screens.WikiScreen
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

private val GardenColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // Verde para botones y elementos principales
    background = Color(0xFFF0F4E8), // Fondo verde claro, como un campo fresco
    surface = Color.White, // Blanco para las tarjetas y superficies
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

enum class Screen {
    Login,
    Home,
    Wiki,
    GardenManagement
}

@Composable
@Preview
fun LogIn(
    onGoogleLogin: (onResult: (Boolean) -> Unit) -> Unit = { _ -> },
    onSetupViewModel: (LoginViewModel) -> Unit = {}
) {
    MaterialTheme(colorScheme = GardenColorScheme) {
        val viewModel = remember { LoginViewModel() }
        var currentScreen by remember { mutableStateOf(Screen.Login) }

        // Si el usuario cierra sesión, volvemos a la pantalla de login
        if (!viewModel.isLoggedIn) {
            currentScreen = Screen.Login
        }

        LaunchedEffect(Unit) {
            onSetupViewModel(viewModel)
        }

        when (currentScreen) {
            Screen.Login -> {
                LoginScreen(
                    viewModel = viewModel,
                    onGoogleLoginRequest = {
                        onGoogleLogin { success ->
                            if (success) {
                                viewModel.onGoogleLogin()
                                currentScreen = Screen.Home
                            } else {
                                println("El usuario canceló o falló el login")
                            }
                        }
                    }
                )
            }
            Screen.Home -> {
                HomeScreen(
                    onLogout = { viewModel.logout() },
                    viewModel = viewModel,
                    navigateToGardenManagement = { currentScreen = Screen.GardenManagement },
                    navigateToWiki = { currentScreen = Screen.Wiki }
                )
            }
            Screen.Wiki -> {
                WikiScreen(
                    onLogout = { viewModel.logout() },
                    viewModel = viewModel,
                    //onNavigateBack = { currentScreen = Screen.Home }
                )
            }
            Screen.GardenManagement -> {
                GardenScreen(
                    //onNavigateBack = { currentScreen = Screen.Home }
                )
            }
        }
    }
}