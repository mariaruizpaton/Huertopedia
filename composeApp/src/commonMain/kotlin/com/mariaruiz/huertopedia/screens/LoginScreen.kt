package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import huertopedia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleLoginRequest: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4E8))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (viewModel.isRegisterMode) stringResource(Res.string.login_create_account) else stringResource(Res.string.login_welcome),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.errorMessage != null) {
            val errorText = when(viewModel.errorMessage) {
                "error_invalid_credentials" -> stringResource(Res.string.error_invalid_credentials)
                "error_user_not_found" -> stringResource(Res.string.error_user_not_found)
                "error_email_already_in_use" -> stringResource(Res.string.error_email_already_in_use)
                "error_weak_password" -> stringResource(Res.string.error_weak_password)
                "error_invalid_email" -> stringResource(Res.string.error_invalid_email)
                "error_network" -> stringResource(Res.string.error_network)
                else -> stringResource(Res.string.error_unknown)
            }

            Text(
                text = errorText,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón Google
        Button(
            onClick = {
                focusManager.clearFocus()
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

        // Tabs Login/Registro
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.isRegisterMode = false 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text(stringResource(Res.string.login_signin)) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.isRegisterMode = true 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray
                )
            ) { Text(stringResource(Res.string.login_register)) }
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
                        OutlinedTextField(
                            value = viewModel.name ?: "",
                            onValueChange = { viewModel.name = it },
                            label = { Text(stringResource(Res.string.login_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text(stringResource(Res.string.login_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = { Text(stringResource(Res.string.login_password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            focusManager.clearFocus()
                            viewModel.onAceptarClick() 
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Aceptar
        Button(
            onClick = { 
                focusManager.clearFocus()
                viewModel.onAceptarClick() 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(Res.string.login_accept))
        }
    }
}
