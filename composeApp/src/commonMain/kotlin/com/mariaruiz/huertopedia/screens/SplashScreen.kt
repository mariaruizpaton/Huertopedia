package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.i18n.LocalStrings

/**
 * Composable para la pantalla de carga (Splash Screen).
 *
 * Muestra el nombre de la aplicación y un indicador de progreso circular mientras se cargan
 * los datos iniciales o se determina el estado de autenticación del usuario.
 */
@Composable
fun SplashScreen() {
    val strings = LocalStrings.current
    
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // Nombre de la app obtenido de las cadenas de recursos localizadas
        Text(
            text = strings.appName, 
            color = Color.White, 
            style = MaterialTheme.typography.headlineLarge
        )
        
        // Indicador de progreso en la parte inferior
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
            color = Color.White
        )
    }
}
