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
            // --- HEADER: LOGO DE LA APP (AGRANDADO Y SIN FONDO) ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp), // Aumentado de 100 a 120
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo_app),
                        contentDescription = "Logo Huertopedia",
                        modifier = Modifier.fillMaxSize() // Ahora ocupa todo el Box sin recortes circulares
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

            // --- DESCRIPCIÓN LOCALIZADA ---
            Text(
                text = strings.aboutDescription,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // --- TARJETA DE DESARROLLADOR ---
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

            // --- BOTONES DE ENLACES ---
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

            // --- pie de página ---
            Text(
                text = strings.aboutCopyright,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
