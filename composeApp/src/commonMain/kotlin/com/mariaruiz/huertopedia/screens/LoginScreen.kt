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
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.repositories.LanguageRepository

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    languageRepository: LanguageRepository,
    onGoogleLoginRequest: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val strings = LocalStrings.current
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F4E8))) {
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (viewModel.isRegisterMode) strings.loginCreateAccount else strings.loginWelcome,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.errorMessage != null) {
                // CAMBIO: Ahora mapeamos a las strings del sistema Type-Safe
                val errorText = when(viewModel.errorMessage) {
                    "error_invalid_credentials" -> strings.errorInvalidCredentials
                    "error_user_not_found" -> strings.errorUserNotFound
                    "error_email_already_in_use" -> strings.errorEmailAlreadyInUse
                    "error_weak_password" -> strings.errorWeakPassword
                    "error_invalid_email" -> strings.errorInvalidEmail
                    "error_network" -> strings.errorNetwork
                    "error_fields_empty" -> strings.errorFieldsEmpty
                    "error_name_empty" -> strings.errorNameEmpty
                    else -> strings.errorUnknown
                }

                Text(
                    text = errorText,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onGoogleLoginRequest()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Canvas(modifier = Modifier.width(24.dp).height(24.dp)) {
                        val width = size.width
                        val height = size.height
                        val strokeWidth = width * 0.22f
                        drawArc(Color(0xFFEA4335), 180f, 145f, false, style = Stroke(strokeWidth))
                        drawArc(Color(0xFFFBBC05), 140f, 40f, false, style = Stroke(strokeWidth))
                        drawArc(Color(0xFF34A853), 45f, 95f, false, style = Stroke(strokeWidth))
                        drawArc(Color(0xFF4285F4), 0f, 45f, false, style = Stroke(strokeWidth))
                        drawLine(Color(0xFF4285F4), androidx.compose.ui.geometry.Offset(width * 1.104f, height / 2), androidx.compose.ui.geometry.Offset(width * 0.55f, height / 2), strokeWidth)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(strings.loginGoogle, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { focusManager.clearFocus(); viewModel.isRegisterMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray)
                ) { Text(strings.loginSignIn) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { focusManager.clearFocus(); viewModel.isRegisterMode = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isRegisterMode) Color(0xFF4CAF50) else Color.Gray)
                ) { Text(strings.loginRegister) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnimatedVisibility(visible = viewModel.isRegisterMode) {
                        Column {
                            OutlinedTextField(
                                value = viewModel.name ?: "",
                                onValueChange = { viewModel.name = it },
                                label = { Text(strings.loginName) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text(strings.loginEmail) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text(strings.loginPassword) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.onAceptarClick() })
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { focusManager.clearFocus(); viewModel.onAceptarClick() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text(strings.loginAccept) }
        }
    }
}
