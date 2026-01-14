package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleLoginRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4E8)) // Tu color de fondo
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (viewModel.isRegisterMode) "Crear Cuenta" else "Bienvenido",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        // AÑADE ESTO PARA VER LOS ERRORES
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // -----------------------------------------------------------
        // BOTÓN DE GOOGLE (Con el logo dibujado)
        // -----------------------------------------------------------
        Button(
            onClick = {
                onGoogleLoginRequest()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // AQUÍ ESTÁ EL CÓDIGO DEL CANVAS QUE TE FALTABA
                Canvas(modifier = Modifier.width(24.dp).height(24.dp)) {
                    val width = size.width
                    val height = size.height
                    val strokeWidth = width * 0.22f

                    // Rojo
                    drawArc(
                        color = Color(0xFFEA4335),
                        startAngle = 180f,
                        sweepAngle = 145f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    // Amarillo
                    drawArc(
                        color = Color(0xFFFBBC05),
                        startAngle = 140f,
                        sweepAngle = 40f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    // Verde
                    drawArc(
                        color = Color(0xFF34A853),
                        startAngle = 45f,
                        sweepAngle = 95f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    // Azul
                    drawArc(
                        color = Color(0xFF4285F4),
                        startAngle = 0f,
                        sweepAngle = 45f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    // Barra azul horizontal
                    drawLine(
                        color = Color(0xFF4285F4),
                        start = androidx.compose.ui.geometry.Offset(x = width * 1.104f, y = height / 2),
                        end = androidx.compose.ui.geometry.Offset(x = width * 0.55f, y = height / 2),
                        strokeWidth = strokeWidth
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text("Acceder con Google", color = Color.Gray)
            }
        }
        // -----------------------------------------------------------

        Spacer(modifier = Modifier.height(26.dp))

        // Pestañas Login/Registro
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { viewModel.isRegisterMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text("Iniciar sesión") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.isRegisterMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text("Registrarse") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedVisibility(visible = viewModel.isRegisterMode) {
                    Column {
                        viewModel.name?.let {
                            OutlinedTextField(
                                value = it,
                                onValueChange = { viewModel.name = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Aceptar
        Button(
            onClick = { viewModel.onAceptarClick() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Aceptar")
        }
    }
}