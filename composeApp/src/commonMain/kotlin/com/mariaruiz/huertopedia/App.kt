package com.mariaruiz.huertopedia

// IMPORTS DE COMPOSE Y MATERIAL3 (Estos son los que te fallaban)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// IMPORTS DE TUS PANTALLAS Y VIEWMODELS
import com.mariaruiz.huertopedia.screens.GardenScreen
import com.mariaruiz.huertopedia.screens.HomeScreen
import com.mariaruiz.huertopedia.screens.LoginScreen
import com.mariaruiz.huertopedia.screens.WikiScreen
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.viewmodel.WikiViewModel

// IMPORT DE PREVIEW
import org.jetbrains.compose.ui.tooling.preview.Preview

// COLORES (Definidos aquí para evitar fallos de imports externos)
private val GardenColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    background = Color(0xFFF0F4E8),
    surface = Color.White,
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
fun App(
    onGoogleLogin: (onResult: (Boolean) -> Unit) -> Unit = { _ -> },
    onSetupViewModel: (LoginViewModel) -> Unit = {},
    onSetupWikiViewModel: (WikiViewModel) -> Unit = {}
) {
    MaterialTheme(colorScheme = GardenColorScheme) {
        // ViewModels
        val viewModel = remember { LoginViewModel() }
        val wikiViewModel = remember { WikiViewModel() }

        // Estado
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
        var currentScreen by remember { mutableStateOf(Screen.Login) }

        // Efectos
        LaunchedEffect(isLoggedIn) {
            currentScreen = if (isLoggedIn) Screen.Home else Screen.Login
        }

        LaunchedEffect(Unit) {
            onSetupViewModel(viewModel)
            onSetupWikiViewModel(wikiViewModel)
        }

        // Navegación
        when (currentScreen) {
            Screen.Login -> LoginScreen(
                viewModel = viewModel,
                onGoogleLoginRequest = {
                    onGoogleLogin { success ->
                        if (success) currentScreen = Screen.Home
                    }
                }
            )
            Screen.Home -> HomeScreen(
                onLogout = { viewModel.logout() },
                viewModel = viewModel,
                navigateToGardenManagement = { currentScreen = Screen.GardenManagement },
                navigateToWiki = { currentScreen = Screen.Wiki }
            )
            Screen.Wiki -> WikiScreen(
                onLogout = { viewModel.logout() },
                onBack = { currentScreen = Screen.Home },
                viewModel = viewModel,
                wikiViewModel = wikiViewModel
            )
            Screen.GardenManagement -> GardenScreen(
                onLogout = { viewModel.logout() },
                onBack = { currentScreen = Screen.Home },
                viewModel = viewModel
            )
        }
    }
}