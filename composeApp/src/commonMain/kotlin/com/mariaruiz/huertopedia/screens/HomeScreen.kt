package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.utils.rememberMapHandler
import com.mariaruiz.huertopedia.utils.toHumanDateTimeString
import org.jetbrains.compose.resources.painterResource
import huertopedia.composeapp.generated.resources.Res
import huertopedia.composeapp.generated.resources.logo_app

/**
 * Composable para la pantalla principal de la aplicación.
 *
 * Muestra una bienvenida personalizada, tarjetas de navegación a las secciones principales
 * (Huerto, Wiki, Mapa), y la última actividad registrada en el diario de cultivo.
 * También proporciona un menú desplegable para acceder al perfil y a la pantalla "Acerca de".
 *
 * @param onLogout Callback para gestionar el cierre de sesión.
 * @param viewModel Instancia de [LoginViewModel] para acceder a los datos del usuario.
 * @param gardenViewModel Instancia de [GardenViewModel] para acceder a los datos del huerto.
 * @param navigateToGardenManagement Callback para navegar a la gestión del huerto.
 * @param navigateToWiki Callback para navegar a la enciclopedia.
 * @param navigateToProfile Callback para navegar al perfil del usuario.
 * @param navigateToAbout Callback para navegar a la pantalla "Acerca de".
 */
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

    val lastActivity by gardenViewModel.globalLastActivity.collectAsState(initial = null)
    val mapHandler = rememberMapHandler()
    val scrollState = rememberScrollState()

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
                            leadingIcon = {
                                Image(
                                    painter = painterResource(Res.drawable.logo_app),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                )
                            },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.homeWelcome.replace("{0}", viewModel.name ?: strings.loginName),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Mapas (Traducida)
            HomeCard(
                title = strings.homeMapCard,
                description = strings.homeMapDesc,
                iconEmoji = "🗺️",
                onClick = {
                    val query = if (langCode == "es") "viveros y tiendas de plantas" else "plant nurseries and stores"
                    mapHandler(query)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                val activityText = if (lastActivity != null) {
                    val tipo = lastActivity?.eventType?.get(langCode) ?: strings.homeActivityDefault
                    val desc = lastActivity?.notes?.get(langCode)?.take(20) ?: ""
                    val fechaStr = lastActivity!!.timestamp.toHumanDateTimeString()

                    "$tipo: $desc ($fechaStr)"
                } else {
                    strings.homeNoActivity
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

/**
 * Composable reutilizable para mostrar una tarjeta de navegación en la pantalla principal.
 *
 * @param title El título principal de la tarjeta.
 * @param description Una breve descripción de la funcionalidad.
 * @param iconEmoji Un emoji representativo que se muestra en un recuadro.
 * @param onClick La acción a ejecutar cuando se hace clic en la tarjeta.
 */
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
