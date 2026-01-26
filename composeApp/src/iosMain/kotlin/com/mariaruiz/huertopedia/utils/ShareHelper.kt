package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberShareHandler(): (String) -> Unit {
    return {
        // Aquí iría la lógica de iOS en el futuro. Por ahora no hace nada.
        println("Compartir no implementado en iOS aún: $it")
    }
}