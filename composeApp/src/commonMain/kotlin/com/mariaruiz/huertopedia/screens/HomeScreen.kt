package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.theme.GardenGreen
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import huertopedia.composeapp.generated.resources.Res
import huertopedia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: LoginViewModel,
    navigateToGardenManagement: () -> Unit,
    navigateToWiki: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 20.sp)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.home_btn_logout)) },
                            onClick = {
                                onLogout()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera
            Text(
                text = stringResource(Res.string.home_welcome, viewModel.name ?: "Usuario"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stringResource(Res.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta 1: Gestión del Huerto
            HomeCard(
                title = stringResource(Res.string.home_card_garden_title),
                description = stringResource(Res.string.home_card_garden_desc),
                iconEmoji = "🪴",
                onClick = navigateToGardenManagement
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 2: Enciclopedia de Cultivos
            HomeCard(
                title = stringResource(Res.string.home_card_wiki_title),
                description = stringResource(Res.string.home_card_wiki_desc),
                iconEmoji = "📖",
                onClick = navigateToWiki
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pie de página: Última actividad
            Text(
                text = stringResource(Res.string.home_last_activity),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .border(1.dp, GardenGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Text(
                    text = stringResource(Res.string.home_activity_sample),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = GardenGreen,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun HomeCard(
    title: String,
    description: String,
    iconEmoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
