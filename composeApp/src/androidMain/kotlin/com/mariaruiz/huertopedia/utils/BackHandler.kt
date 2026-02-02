/**
 * Implementación real (`actual`) para Android de la función `BackHandler`.
 *
 * Utiliza el `BackHandler` que proporciona la librería `androidx.activity.compose` para interceptar
 * el evento del botón "Atrás" del sistema.
 *
 * @param isEnabled Si es `true`, el manejador estará activo. Si es `false`, estará desactivado.
 * @param onBack La lambda que se ejecutará cuando se presione el botón "Atrás" y el manejador esté habilitado.
 */
package com.mariaruiz.huertopedia.utils

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled = isEnabled, onBack = onBack)
}