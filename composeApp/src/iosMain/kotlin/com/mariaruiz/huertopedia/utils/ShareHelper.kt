package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

/**
 * Composable `actual` para iOS que proporciona un manejador para compartir contenido.
 *
 * **Nota:** Esta funcionalidad aún no está implementada en iOS.
 *
 * @return Una función que toma un `String` para compartir. Actualmente, solo imprime un mensaje en la consola.
 */
@Composable
actual fun rememberShareHandler(): (String) -> Unit {
    return {
        // Aquí iría la lógica de iOS en el futuro. Por ahora no hace nada.
        println("Compartir no implementado en iOS aún: $it")
    }
}