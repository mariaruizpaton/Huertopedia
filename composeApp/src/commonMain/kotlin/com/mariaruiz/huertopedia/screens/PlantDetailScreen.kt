package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Plant
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.mariaruiz.huertopedia.utils.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import huertopedia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(plant: Plant, onBack: () -> Unit) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.detail_title_page)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.detail_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // CABECERA CON IMAGEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFE8F5E9))
            ) {
                if (!plant.imagenUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = asyncPainterResource(data = plant.imagenUrl),
                        contentDescription = plant.nombreComun,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Park,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).align(Alignment.Center),
                        tint = Color.Gray
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TÍTULO Y CATEGORÍA
                Text(
                    text = plant.nombreComun,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Surface(
                    color = Color(0xFFC8E6C9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    val categoryResource = when(plant.categoria) {
                        "Hortalizas" -> stringResource(Res.string.wiki_cat_veg)
                        "Frutas" -> stringResource(Res.string.wiki_cat_fruit)
                        "Hierbas" -> stringResource(Res.string.wiki_cat_herbs)
                        else -> plant.categoria
                    }
                    Text(
                        text = categoryResource,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1B5E20)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = stringResource(Res.string.detail_harvest),
                        value = stringResource(Res.string.detail_harvest_days, plant.diasCosecha.toString())
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        label = stringResource(Res.string.detail_watering),
                        value = plant.frecuenciaRiego,
                        iconColor = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DetailSection(
                    title = stringResource(Res.string.detail_soil), icon = Icons.Default.Landscape, color = Color(
                        0xFFA34400
                    )
                ) {
                    Text(text = plant.tipoSustrato.ifEmpty { stringResource(Res.string.detail_not_specified) })
                }

                DetailSection(title = stringResource(Res.string.detail_season), icon = Icons.Default.CalendarMonth) {
                    Text(
                        text = plant.temporadaSiembra.joinToString(", ")
                            .ifEmpty { stringResource(Res.string.detail_not_specified) })
                }

                DetailSection(
                    title = stringResource(Res.string.detail_friends),
                    icon = Icons.Default.ThumbUp,
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = plant.plantasAmigables.joinToString(", ")
                            .ifEmpty { stringResource(Res.string.detail_none_known) })
                }

                DetailSection(
                    title = stringResource(Res.string.detail_enemies),
                    icon = Icons.Default.ThumbDown,
                    color = Color(0xFFE53935)
                ) {
                    Text(
                        text = plant.plantasEnemigas.joinToString(", ")
                            .ifEmpty { stringResource(Res.string.detail_none_known) })
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


@Composable
fun InfoCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color = Color(0xFF2E7D32)
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Composable
fun DetailSection(
    title: String,
    icon: ImageVector,
    color: Color = Color.Gray,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}
