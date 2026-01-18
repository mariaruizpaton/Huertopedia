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
import huertopedia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleLoginRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4E8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (viewModel.isRegisterMode) stringResource(Res.string.login_create_account) else stringResource(Res.string.login_welcome),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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
                Canvas(modifier = Modifier.width(24.dp).height(24.dp)) {
                    val width = size.width
                    val height = size.height
                    val strokeWidth = width * 0.22f

                    drawArc(
                        color = Color(0xFFEA4335),
                        startAngle = 180f,
                        sweepAngle = 145f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = Color(0xFFFBBC05),
                        startAngle = 140f,
                        sweepAngle = 40f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = Color(0xFF34A853),
                        startAngle = 45f,
                        sweepAngle = 95f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = Color(0xFF4285F4),
                        startAngle = 0f,
                        sweepAngle = 45f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawLine(
                        color = Color(0xFF4285F4),
                        start = androidx.compose.ui.geometry.Offset(x = width * 1.104f, y = height / 2),
                        end = androidx.compose.ui.geometry.Offset(x = width * 0.55f, y = height / 2),
                        strokeWidth = strokeWidth
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(Res.string.login_google), color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { viewModel.isRegisterMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text(stringResource(Res.string.login_signin)) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.isRegisterMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text(stringResource(Res.string.login_register)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                                label = { Text(stringResource(Res.string.login_name)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text(stringResource(Res.string.login_email)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = { Text(stringResource(Res.string.login_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onAceptarClick() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(Res.string.login_accept))
        }
    }
}
