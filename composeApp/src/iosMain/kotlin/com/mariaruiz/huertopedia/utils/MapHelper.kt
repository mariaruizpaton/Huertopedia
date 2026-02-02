/**
 * Implementación real (`actual`) para iOS de la función `rememberMapHandler`.
 *
 * Esta función crea y recuerda una lambda que, al ser invocada con una consulta de búsqueda,
 * abre la aplicación de Apple Maps para mostrar los resultados.
 *
 * @return Una función `(String) -> Unit` que abre Apple Maps con la consulta dada.
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberMapHandler(): (String) -> Unit {
    return remember {
        { query ->
            // Se codifica la consulta para que sea segura en una URL.
            val encodedQuery = query.replace(" ", "+")
            // Se crea la URL para abrir Apple Maps con la consulta.
            val url = NSURL.URLWithString("http://maps.apple.com/?q=$encodedQuery")

            // Se comprueba si se puede abrir la URL y, si es así, se abre.
            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
}