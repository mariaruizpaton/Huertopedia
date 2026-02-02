/**
 * Implementación real (`actual`) para Android de la función `rememberMapHandler`.
 *
 * Esta función crea y recuerda una lambda que, al ser invocada con una consulta de búsqueda,
 * abre la aplicación de Google Maps para mostrar los resultados. Si Google Maps no está instalado,
 * recurre a abrir una búsqueda en el navegador web.
 *
 * @return Una función `(String) -> Unit` que abre los mapas con la consulta dada.
 */
package com.mariaruiz.huertopedia.utils

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberMapHandler(): (String) -> Unit {
    val context = LocalContext.current

    return remember {
        { query ->
            // Se crea un Intent para buscar en Google Maps.
            val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Se comprueba si la app de Google Maps está disponible.
            // Si lo está, se lanza el Intent para abrirla.
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Si no, se crea un Intent para abrir el navegador con la búsqueda en Google Maps.
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query"))
                context.startActivity(browserIntent)
            }
        }
    }
}