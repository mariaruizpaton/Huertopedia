package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberMapHandler(): (String) -> Unit {
    return remember {
        { query ->
            // Codificamos la búsqueda para URL (espacios -> %20, etc)
            val encodedQuery = query.replace(" ", "+")
            // Abrimos Apple Maps buscando lo que queramos
            val url = NSURL.URLWithString("http://maps.apple.com/?q=$encodedQuery")

            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
}