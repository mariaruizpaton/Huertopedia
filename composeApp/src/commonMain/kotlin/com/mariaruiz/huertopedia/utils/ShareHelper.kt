package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

// Declaramos que "esperamos" que cada plataforma nos dé una función para compartir
@Composable
expect fun rememberShareHandler(): (String) -> Unit