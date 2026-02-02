/**
 * Este archivo define la funcionalidad esperada para compartir contenido desde la aplicación.
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

/**
 * Una función Composable `expect` que se implementará en cada plataforma para proporcionar una
 * forma de compartir contenido de texto.
 *
 * Devuelve una función lambda que toma el [String] del mensaje a compartir y no devuelve nada.
 * La implementación `actual` en cada plataforma se encargará de invocar el diálogo nativo
 * para compartir.
 *
 * @return Una función lambda `(String) -> Unit` que puede ser invocada para iniciar la acción de compartir.
 */
@Composable
expect fun rememberShareHandler(): (String) -> Unit