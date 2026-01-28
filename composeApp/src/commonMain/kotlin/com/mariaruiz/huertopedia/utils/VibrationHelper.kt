package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

// Declaramos que esperamos una función que haga vibrar el móvil
@Composable
expect fun rememberVibrationHandler(): (Long) -> Unit