package com.mariaruiz.huertopedia

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.mariaruiz.huertopedia.screens.*
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.viewmodel.WikiViewModel
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.model.Planter
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    User,
    Home,
    Wiki,
    GardenManagement,
    PlantDetail,
    CropLog

}

@Composable
@Preview
fun App(
    onGoogleLogin: (onResult: (Boolean) -> Unit) -> Unit = { _ -> },
    onSetupViewModel: (LoginViewModel) -> Unit = {},
    onSetupWikiViewModel: (WikiViewModel) -> Unit = {}
) {
    MaterialTheme(colorScheme = GardenColorScheme) {
        val viewModel = remember { LoginViewModel() }
        val wikiViewModel = remember { WikiViewModel() }
        val gardenViewModel = remember { GardenViewModel() }
        val selectedPlanter = remember { mutableStateOf<Planter?>(null) }

        // Usamos '=' para usar .value y eliminar los warnings del IDE
        val selectedPlant = remember { mutableStateOf<Plant?>(null) }
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
        val currentScreen = remember { mutableStateOf(Screen.Login) }

        LaunchedEffect(isLoggedIn) {
            currentScreen.value = if (isLoggedIn) Screen.Home else Screen.Login
        }

        LaunchedEffect(Unit) {
            onSetupViewModel(viewModel)
            onSetupWikiViewModel(wikiViewModel)
        }

        when (currentScreen.value) {
            Screen.Login -> {
                LoginScreen(
                    viewModel = viewModel,
                    onGoogleLoginRequest = {
                        onGoogleLogin { success ->
                            if (success) currentScreen.value = Screen.Home
                        }
                    }
                )
            }
            Screen.User -> {
                UserScreen(
                    onLogout = { viewModel.logout() },
                    onBack = { currentScreen.value = Screen.Home },
                    viewModel = viewModel
                )
            }
            Screen.Home -> {
                HomeScreen(
                    onLogout = { viewModel.logout() },
                    viewModel = viewModel,
                    navigateToGardenManagement = { currentScreen.value = Screen.GardenManagement },
                    navigateToWiki = { currentScreen.value = Screen.Wiki },
                    navigateToProfile = { currentScreen.value = Screen.User}
                )
            }
            Screen.Wiki -> {
                WikiScreen(
                    onBack = { currentScreen.value = Screen.Home },
                    wikiViewModel = wikiViewModel,
                    onPlantClick = { plant ->
                        selectedPlant.value = plant
                        currentScreen.value = Screen.PlantDetail
                    }

                )
            }
            Screen.GardenManagement -> {
                GardenScreen(
                    onBack = { currentScreen.value = Screen.Home },
                    viewModel = viewModel,
                    gardenViewModel = gardenViewModel,
                    onNavigateToLog = { planter ->
                        selectedPlanter.value = planter
                        currentScreen.value = Screen.CropLog
                    }
                )
            }
            Screen.PlantDetail -> {
                selectedPlant.value?.let { plant ->
                    PlantDetailScreen(
                        plant = plant,
                        onBack = { currentScreen.value = Screen.Wiki }
                    )
                }
            }
            Screen.CropLog -> {
                selectedPlanter.value?.let { planter ->
                    CropLogScreen(
                        planter = planter,
                        onBack = { currentScreen.value = Screen.GardenManagement },
                        gardenViewModel = gardenViewModel
                    )
                }
            }
        }
    }
}
