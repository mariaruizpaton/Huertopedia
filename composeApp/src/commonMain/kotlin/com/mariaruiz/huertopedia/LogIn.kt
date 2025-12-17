package com.mariaruiz.huertopedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// ... (El colorScheme y el enum FormState se mantienen igual)
private val GardenColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // Verde para botones y elementos principales
    background = Color(0xFFF0F4E8), // Fondo verde claro, como un campo fresco
    surface = Color.White, // Blanco para las tarjetas y superficies
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private enum class FormState {
    LOGIN,
    REGISTER
}

@Composable
@Preview
fun LogIn() {
    MaterialTheme(colorScheme = GardenColorScheme) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var currentForm by remember { mutableStateOf(FormState.LOGIN) } // Estado inicial: Login

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (currentForm == FormState.LOGIN) "Iniciar Sesión" else "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Botones para cambiar entre Iniciar Sesión y Registrarse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { currentForm = FormState.LOGIN },
                    colors = if (currentForm == FormState.LOGIN) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Iniciar sesión")
                }
                Button(
                    onClick = { currentForm = FormState.REGISTER },
                    colors = if (currentForm == FormState.REGISTER) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recuadro para el formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // El campo de Nombre solo aparece si estamos en modo REGISTRO
                    if (currentForm == FormState.REGISTER) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // El campo de contraseña solo aparece si estamos en modo REGISTRO
                    // o si estamos en LOGIN. Ahora es visible en ambos.
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de acción principal (Confirmar Inicio de Sesión o Registro)
            Button(
                onClick = {
                    if (currentForm == FormState.LOGIN) {
                        // TODO: Lógica de inicio de sesión con email y contraseña
                    } else {
                        // TODO: Lógica para registrar al nuevo usuario
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    if (currentForm == FormState.LOGIN) "Acceder" else "Confirmar Registro",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    "o",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Divider(modifier = Modifier.weight(1f))
            }

            // Botón de Iniciar Sesión con Google (versión texto)
            OutlinedButton(
                onClick = { /* TODO: Lógica de inicio de sesión con Google */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Aquí podrías añadir la imagen del logo si la tienes en tus recursos
                // Image(painter = painterResource(Res.drawable.google_logo), ...)
                Text("Continuar con Google")
            }
        }
    }
}
