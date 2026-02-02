/**
 * Este archivo define la funcionalidad esperada para manejar el evento del botón "Atrás".
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

/**
 * Un Composable `expect` para manejar las pulsaciones del botón "Atrás" del sistema.
 *
 * Esta función permite interceptar el evento de retroceso y ejecutar una acción personalizada.
 * Debe ser implementada (`actual`) en cada plataforma específica (Android, iOS) para
 * integrarse con el mecanismo de retroceso nativo.
 *
 * @param isEnabled Si es `true`, el manejador estará activo y interceptará el evento de retroceso.
 *                  Si es `false`, se desactivará. El valor predeterminado es `true`.
 * @param onBack La expresión lambda que se ejecutará cuando se presione el botón "Atrás"
 *               y el manejador esté habilitado.
 */
@Composable
expect fun BackHandler(isEnabled: Boolean = true, onBack: () -> Unit)