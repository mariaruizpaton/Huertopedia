package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.i18n.LocalStrings
import org.jetbrains.compose.resources.painterResource
import huertopedia.composeapp.generated.resources.Res
import huertopedia.composeapp.generated.resources.logo_app

/**
 * Composable para la pantalla "Acerca de".
 *
 * Muestra información sobre la aplicación, como el logo, nombre, versión, una breve descripción,
 * los desarrolladores y enlaces al código fuente y a la política de privacidad.
 *
 * @param onBack Callback para gestionar la navegación hacia atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.aboutTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Encabezado: Logo y Nombre de la App ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(120.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo_app),
                        contentDescription = "Logo Huertopedia",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Huertopedia",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.aboutVersion,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // --- Descripción de la App ---
            Text(
                text = strings.aboutDescription,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // --- Tarjeta de Desarrolladores ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                ListItem(
                    headlineContent = { Text(strings.aboutDeveloper) },
                    supportingContent = { Text("María Ruiz y Silvia Balmaseda", fontWeight = FontWeight.Bold) },
                    leadingContent = {
                        Icon(Icons.Default.Code, null)
                    }
                )
            }

            // --- Botones de Enlaces Externos ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/mariaruizpaton/Huertopedia.git") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.aboutSourceCode)
                }

                TextButton(
                    onClick = { uriHandler.openUri("https://huertopedia.app/privacy") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Policy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.aboutPrivacyPolicy)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Pie de Página ---
            Text(
                text = strings.aboutCopyright,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
