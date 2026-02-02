/**
 * Implementación real (`actual`) para Android de la función `rememberShareHandler`.
 *
 * Esta función crea y recuerda una lambda que, al ser invocada con un texto, utiliza un
 * `Intent.ACTION_SEND` para abrir el diálogo de compartir nativo de Android, permitiendo
 * al usuario compartir el texto a través de otras aplicaciones.
 *
 * @return Una función `(String) -> Unit` que inicia la acción de compartir.
 */
package com.mariaruiz.huertopedia.utils

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareHandler(): (String) -> Unit {
    val context = LocalContext.current
    return remember {
        { text ->
            // Se crea un Intent para compartir texto.
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            // Se crea un selector para que el usuario elija la app con la que compartir.
            val shareIntent = Intent.createChooser(sendIntent, "Compartir mi jardinera")
            // Se inicia la actividad de compartir.
            context.startActivity(shareIntent)
        }
    }
}