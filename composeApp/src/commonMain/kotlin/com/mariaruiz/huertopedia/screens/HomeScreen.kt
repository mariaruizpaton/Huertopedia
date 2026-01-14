package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: LoginViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Fondo blanco
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Hola, ${viewModel.name}!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bienvenido a la aplicación.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botón para salir
        Button(
            onClick = { onLogout() } // Al hacer clic, llamamos a la función que nos pasaron
        ) {
            Text("Cerrar Sesión")
        }
    }
}