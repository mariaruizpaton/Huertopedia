// iosApp/src/iosMain/kotlin/.../MainViewController.kt
package com.mariaruiz.huertopedia

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
// No necesitas imports de Firebase ni Coroutines aquí

fun MainViewController(): UIViewController = ComposeUIViewController {
    // Ya no necesitas 'scope' ni lógica de Firebase aquí.

    App(
        // Pasamos una función vacía, porque el WikiViewModel
        // ya se inicia y carga los datos él solito.
        onSetupWikiViewModel = { viewModel ->
            // No hacer nada. El ViewModel ya tiene su init { fetchPlants() }
        }
    )
}