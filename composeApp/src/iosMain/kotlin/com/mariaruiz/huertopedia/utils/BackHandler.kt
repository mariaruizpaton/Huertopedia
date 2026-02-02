/**
 * Implementación real (`actual`) para iOS de la función `BackHandler`.
 *
 * Esta implementación utiliza el `BackHandler` de `androidx.activity.compose`, que está pensado
 * principalmente para Android. En un entorno de iOS puro, esto no tendría efecto, pero en un
 * proyecto de Compose Multiplatform, la expectativa es que se gestione de forma transparente.
 *
 * @param isEnabled Si es `true`, el manejador estará activo. Si es `false`, estará desactivado.
 * @param onBack La lambda que se ejecutará cuando se presione el botón "Atrás" (o gesto equivalente) y el manejador esté habilitado.
 */
package com.mariaruiz.huertopedia.utils

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    // A pesar de que el nombre sugiere Android, este Composable está disponible
    // en la parte común de Compose y gestiona la pila de "back" de forma abstracta.
    AndroidBackHandler(enabled = isEnabled, onBack = onBack)
}