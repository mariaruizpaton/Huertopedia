package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * Composable para la pantalla de detalle de una planta específica.
 *
 * Muestra toda la información detallada de una [Plant], incluyendo su imagen, nombres,
 * información de cultivo (siembra, recolección, etc.) en tarjetas, y secciones de texto
 * para cuidados, plantas amigables y enemigas. Toda la información textual es localizada.
 *
 * @param plant El objeto [Plant] cuyos detalles se van a mostrar.
 * @param languageRepository Repositorio para obtener el idioma actual y localizar el contenido.
 * @param onBack Callback para navegar hacia atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: Plant, 
    languageRepository: LanguageRepository,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val langCode by languageRepository.currentLanguage.collectAsState()
    
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.detailTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.detailBack)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // --- Cabecera con Imagen ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                if (!plant.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(data = plant.imagenUrl!!),
                        contentDescription = plant.nombreComun.get(langCode),
                        modifier = Modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // --- Títulos Localizados ---
                Text(
                    text = plant.nombreComun.get(langCode).capitalizeFirst(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = plant.nombreCientifico,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // --- Cuadrícula de Información Clave ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.CalendarMonth, strings.detailSowing, plant.siembra.get(langCode))
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Agriculture, strings.detailHarvest, plant.recoleccion.get(langCode))
                    }
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Thermostat, strings.detailTemperature, plant.temperaturaOptima, Color(0xFFFF9800))
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Yard, strings.detailFertilizer, plant.abono.get(langCode), Color(0xFF8BC34A))
                    }
                    InfoCard(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Icons.Default.WaterDrop, strings.detailWatering, plant.riego.get(langCode), Color(0xFF2196F3))
                }

                Spacer(Modifier.height(24.dp))

                // --- Secciones de Texto Largo ---
                DetailSection(strings.detailCare, Icons.Default.VerifiedUser, MaterialTheme.colorScheme.primary) {
                    val text = plant.cuidados.get(langCode).ifEmpty { strings.detailNotSpecified }
                    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                DetailSection(strings.detailFriends, Icons.Default.ThumbUp, Color(0xFF4CAF50)) {
                    val friends = plant.plantasAmigables.map { it.get(langCode) }.joinToString(", ")
                    Text(friends.ifEmpty { strings.detailNoneKnown }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                DetailSection(strings.detailEnemies, Icons.Default.ThumbDown, Color(0xFFE53935)) {
                    val enemies = plant.plantasEnemigas.map { it.get(langCode) }.joinToString(", ")
                    Text(enemies.ifEmpty { strings.detailNoneKnown }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Composable reutilizable para mostrar una tarjeta de información clave en la pantalla de detalle.
 *
 * @param modifier Modificador para personalizar la tarjeta.
 * @param icon Icono que representa la información.
 * @param label Etiqueta descriptiva del dato.
 * @param value El valor del dato a mostrar.
 * @param iconColor Color del icono.
 */
@Composable
fun InfoCard(modifier: Modifier, icon: ImageVector, label: String, value: String, iconColor: Color = Color(0xFF4CAF50)) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, Modifier.size(24.dp), iconColor)
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Text(text = value.ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Composable reutilizable para una sección de texto detallado con un título e icono.
 *
 * @param title Título de la sección.
 * @param icon Icono para la sección.
 * @param color Color del icono y la línea divisoria.
 * @param content El contenido Composable de la sección.
 */
@Composable
fun DetailSection(title: String, icon: ImageVector, color: Color = Color.Gray, content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp), color)
            Spacer(Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(4.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Box(Modifier.padding(12.dp)) { content() }
        }
    }
}

/**
 * Pone en mayúscula la primera letra de una cadena de texto.
 */
private fun String.capitalizeFirst(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
