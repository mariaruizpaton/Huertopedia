package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.WikiViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WikiScreen(
    onBack: () -> Unit,
    wikiViewModel: WikiViewModel,
    languageRepository: LanguageRepository,
    onPlantClick: (Plant) -> Unit
) {
    val strings = LocalStrings.current
    val currentLangCode by languageRepository.currentLanguage.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todo") }
    val plants by wikiViewModel.plants.collectAsState()

    // Actualizamos el filtrado cuando cambia el texto, la categoría O el idioma
    LaunchedEffect(searchQuery, selectedFilter, currentLangCode) {
        wikiViewModel.filterPlants(searchQuery, selectedFilter, currentLangCode)
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.wikiTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.detailBack
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(strings.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val categories = listOf(
                        strings.wikiCategoryAll to "Todo",
                        strings.wikiCategoryVegetables to "Hortalizas",
                        strings.wikiCategoryFruits to "Frutas",
                        strings.wikiCategoryHerbs to "Hierbas"
                    )
                    categories.forEach { (label, value) ->
                        FilterChip(
                            selected = value == selectedFilter,
                            onClick = { selectedFilter = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(plants) { plant ->
                    PlantCard(
                        plant = plant, 
                        langCode = currentLangCode, 
                        onClick = { onPlantClick(plant) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantCard(plant: Plant, langCode: String, onClick: () -> Unit) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
            
            if (!plant.imagenUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(data = plant.imagenUrl),
                    contentDescription = plant.nombreComun.get(langCode),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium),
                    onFailure = {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(placeholderColor)
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.wikiNoPhoto, 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plant.nombreComun.get(langCode),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = plant.categoria.get(langCode),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
