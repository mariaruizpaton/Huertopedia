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
            // Creamos una URI de tipo geo para buscar lugares cercanos
            val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Intentamos abrir Google Maps, si no tiene, abrimos navegador
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback: abrir en navegador
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query"))
                context.startActivity(browserIntent)
            }
        }
    }
}