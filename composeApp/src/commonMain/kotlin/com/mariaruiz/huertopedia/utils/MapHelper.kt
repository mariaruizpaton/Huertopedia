package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

// Esperamos una función que abra el mapa buscando un término (ej: "viveros")
@Composable
expect fun rememberMapHandler(): (String) -> Unit