package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel,
) {
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Foto de perfil",
            modifier = Modifier.size(128.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = viewModel.name ?: "Usuario")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}