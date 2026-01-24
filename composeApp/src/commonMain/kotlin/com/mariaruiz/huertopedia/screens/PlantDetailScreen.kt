package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.i18n.LocalStrings
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(plant: Plant, onBack: () -> Unit) {
    val strings = LocalStrings.current
    
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
            // IMAGEN DE CABECERA - Fondo adaptativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                if (!plant.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(data = plant.imagenUrl!!),
                        contentDescription = plant.nombreComun,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Park, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp).align(Alignment.Center), 
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // NOMBRES - Color primario dinámico
                Text(
                    text = plant.nombreComun.capitalizeFirst(), 
                    style = MaterialTheme.typography.headlineLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                if (plant.nombreCientifico.isNotEmpty()) {
                    Text(
                        text = plant.nombreCientifico.capitalizeFirst(), 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontStyle = FontStyle.Italic, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // CATEGORÍA - Estilo adaptativo
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer, 
                    shape = RoundedCornerShape(8.dp), 
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    val cat = when(plant.categoria) {
                        "Hortalizas" -> strings.wikiCategoryVegetables
                        "Frutas" -> strings.wikiCategoryFruits
                        "Hierbas" -> strings.wikiCategoryHerbs
                        else -> plant.categoria
                    }
                    Text(
                        text = cat, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), 
                        style = MaterialTheme.typography.labelLarge, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.height(24.dp))

                // GRID DE INFORMACIÓN TÉCNICA - Limpieza total de blancos
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.CalendarMonth, strings.detailSowing, plant.siembra.capitalizeFirst())
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Agriculture, strings.detailHarvest, plant.recoleccion.capitalizeFirst())
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Thermostat, strings.detailTemperature, plant.temperaturaOptima.capitalizeFirst(), Color(0xFFFF9800))
                        InfoCard(Modifier.weight(1f).fillMaxHeight(), Icons.Default.Yard, strings.detailFertilizer, plant.abono.capitalizeFirst(), Color(0xFF8BC34A))
                    }

                    InfoCard(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Icons.Default.WaterDrop, strings.detailWatering, plant.riego.capitalizeFirst(), Color(0xFF2196F3))
                }

                Spacer(Modifier.height(24.dp))

                // SECCIONES DE TEXTO LARGO - Fondos adaptados
                DetailSection(strings.detailCare, Icons.Default.VerifiedUser, MaterialTheme.colorScheme.primary) {
                    Text(
                        text = plant.cuidados.capitalizeFirst().ifEmpty { strings.detailNotSpecified },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DetailSection(strings.detailFriends, Icons.Default.ThumbUp, Color(0xFF4CAF50)) {
                    Text(
                        text = plant.plantasAmigables.joinToString(", ").capitalizeFirst().ifEmpty { strings.detailNoneKnown },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DetailSection(strings.detailEnemies, Icons.Default.ThumbDown, Color(0xFFE53935)) {
                    Text(
                        text = plant.plantasEnemigas.joinToString(", ").capitalizeFirst().ifEmpty { strings.detailNoneKnown },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, icon: ImageVector, label: String, value: String, iconColor: Color = Color(0xFF4CAF50)) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(24.dp), iconColor)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = value.ifEmpty { "-" }, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailSection(title: String, icon: ImageVector, color: Color = Color.Gray, content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp), color)
            Spacer(Modifier.width(8.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(Modifier.padding(12.dp)) { content() }
        }
    }
}

fun String.capitalizeFirst(): String {
    if (this.isEmpty()) return this
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
