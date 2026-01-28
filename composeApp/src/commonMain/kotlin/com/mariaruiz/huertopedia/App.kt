package com.mariaruiz.huertopedia

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mariaruiz.huertopedia.i18n.EnStrings
import com.mariaruiz.huertopedia.i18n.EsStrings
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.screens.*
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.viewmodel.WikiViewModel
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import com.mariaruiz.huertopedia.repositories.ThemeRepository
import com.mariaruiz.huertopedia.theme.DarkGardenColors
import com.mariaruiz.huertopedia.theme.LightGardenColors
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen {
    Login, User, Home, Wiki, GardenManagement, PlantDetail, CropLog, About
}

@Composable
@Preview
fun App(
    onGoogleLogin: (onResult: (Boolean) -> Unit) -> Unit = { _ -> },
    onSetupViewModel: (LoginViewModel) -> Unit = {},
    onSetupWikiViewModel: (WikiViewModel) -> Unit = {}
) {
    // 1. Repositorios de Persistencia
    val languageRepository = remember { LanguageRepository() }
    val themeRepository = remember { ThemeRepository() }

    // 2. Estados de Idioma
    val currentLangCode by languageRepository.currentLanguage.collectAsState()
    val strings = when (currentLangCode) {
        "en" -> EnStrings
        else -> EsStrings
    }

    // 3. Estados de Tema (Oscuro/Claro)
    val themePref by themeRepository.themePreference.collectAsState()
    val isDarkTheme = when (themePref) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (isDarkTheme) DarkGardenColors else LightGardenColors

    CompositionLocalProvider(LocalStrings provides strings) {
        MaterialTheme(colorScheme = colors) {
            val viewModel = remember { LoginViewModel() }
            val wikiViewModel = remember { WikiViewModel() }
            val gardenViewModel = remember { GardenViewModel() }
            val selectedPlanter = remember { mutableStateOf<Planter?>(null) }
            val selectedPlant = remember { mutableStateOf<Plant?>(null) }
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val currentScreen = remember { mutableStateOf(Screen.Login) }

            // Sincronización de preferencias al iniciar sesión
            LaunchedEffect(isLoggedIn, viewModel.userId) {
                currentScreen.value = if (isLoggedIn) Screen.Home else Screen.Login
                
                if (isLoggedIn && viewModel.userId.isNotEmpty()) {
                    try {
                        val db = Firebase.firestore
                        val doc = db.collection("usuario").document(viewModel.userId).get()
                        if (doc.exists) {
                            val prefs = doc.get<Map<String, String>>("preferences")
                            prefs?.let {
                                val lang = it["language"]
                                val theme = it["theme"]
                                if (lang != null && lang != currentLangCode) {
                                    languageRepository.setLanguage(lang)
                                }
                                if (theme != null && theme != themePref) {
                                    themeRepository.setTheme(theme)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error al sincronizar preferencias: ${e.message}")
                    }
                }
            }

            LaunchedEffect(Unit) {
                onSetupViewModel(viewModel)
                onSetupWikiViewModel(wikiViewModel)
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (currentScreen.value) {
                    Screen.Login -> {
                        LoginScreen(
                            viewModel = viewModel,
                            languageRepository = languageRepository,
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
                            viewModel = viewModel,
                            languageRepository = languageRepository,
                            themeRepository = themeRepository
                        )
                    }
                    Screen.Home -> {
                        HomeScreen(
                            onLogout = { viewModel.logout() },
                            viewModel = viewModel,
                            gardenViewModel = gardenViewModel,
                            navigateToGardenManagement = { currentScreen.value = Screen.GardenManagement },
                            navigateToWiki = { currentScreen.value = Screen.Wiki },
                            navigateToProfile = { currentScreen.value = Screen.User },
                            navigateToAbout = { currentScreen.value = Screen.About }
                        )
                    }
                    Screen.Wiki -> {
                        WikiScreen(
                            onBack = { currentScreen.value = Screen.Home },
                            wikiViewModel = wikiViewModel,
                            languageRepository = languageRepository,
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
                            languageRepository = languageRepository,
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
                                languageRepository = languageRepository,
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
                    Screen.About -> {
                        AboutScreen(
                            onBack = { currentScreen.value = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}
