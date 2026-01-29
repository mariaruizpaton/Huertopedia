package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.utils.toHumanDateTimeString

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: LoginViewModel,
    gardenViewModel: GardenViewModel,
    navigateToGardenManagement: () -> Unit,
    navigateToWiki: () -> Unit,
    navigateToProfile : () -> Unit,
    navigateToAbout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    val langCode = strings.changeLanguage.takeLast(2).lowercase() // Obtenemos "es" o "en"

    // Observamos la última actividad del ViewModel
    val lastActivity by gardenViewModel.globalLastActivity.collectAsState(initial = null)

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
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
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
                            text = { Text(strings.viewProfile) },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            onClick = {
                                showMenu = false
                                navigateToProfile()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(strings.aboutTitle) },
                            leadingIcon = { Icon(Icons.Default.Info, null) },
                            onClick = {
                                showMenu = false
                                navigateToAbout()
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
            Text(
                text = strings.homeWelcome.replace("{0}", viewModel.name ?: "Usuario"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = strings.welcomeSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            HomeCard(
                title = strings.homeGardenCard,
                description = strings.homeGardenDesc,
                iconEmoji = "🪴",
                onClick = navigateToGardenManagement
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeCard(
                title = strings.homeWikiCard,
                description = strings.homeWikiDesc,
                iconEmoji = "📖",
                onClick = navigateToWiki
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = strings.homeLastActivity,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                // Usamos tus utilidades para formatear la fecha/hora
                val activityText = if (lastActivity != null) {
                    val tipo = lastActivity?.eventType?.get(langCode) ?: "Actividad"
                    val desc = lastActivity?.notes?.get(langCode)?.take(20) ?: ""
                    val fechaStr = lastActivity!!.timestamp.toHumanDateTimeString()

                    "$tipo: $desc ($fechaStr)"
                } else {
                    "No hay actividad reciente"
                }

                Text(
                    text = activityText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
