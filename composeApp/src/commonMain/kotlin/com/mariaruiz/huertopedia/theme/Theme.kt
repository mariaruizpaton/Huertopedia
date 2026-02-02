/**
 * Este archivo define las paletas de colores para los temas claro y oscuro de la aplicación.
 */
package com.mariaruiz.huertopedia.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores para el tema claro de la aplicación.
 */
val LightGardenColors = lightColorScheme(
    primary = Color(0xFF4CAF50),
    background = Color(0xFFF0F4E8),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFFC8E6C9)
)

/**
 * Paleta de colores para el tema oscuro de la aplicación, diseñada para ser agradable a la vista en condiciones de poca luz.
 */
val DarkGardenColors = darkColorScheme(
    primary = Color(0xFF81C784), // Verde más suave para que no brille mucho
    background = Color(0xFF12140E), // Fondo casi negro con un toque verdoso
    surface = Color(0xFF1B1D17),    // Tarjetas un poco más claras que el fondo
    onPrimary = Color(0xFF003300),
    onBackground = Color(0xFFE2E3D8),
    onSurface = Color(0xFFE2E3D8),
    secondaryContainer = Color(0xFF2D3228)
)