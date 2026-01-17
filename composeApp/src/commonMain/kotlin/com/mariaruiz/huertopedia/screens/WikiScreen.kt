package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.viewmodel.WikiViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

// Eliminado ExperimentalResourceApi del OptIn y de los imports
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WikiScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel,
    wikiViewModel: WikiViewModel = remember { WikiViewModel() }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todo") }
    val plants by wikiViewModel.plants.collectAsState()

    LaunchedEffect(searchQuery, selectedFilter) {
        wikiViewModel.filterPlants(searchQuery, selectedFilter)
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wiki") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar plantas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf("Todo", "Hortalizas", "Frutas", "Hierbas")
                    categories.forEach { category ->
                        FilterChip(
                            selected = category == selectedFilter,
                            onClick = { selectedFilter = category },
                            label = { Text(category) }
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
                    PlantCard(plant)
                }
            }
        }
    }
}

@Composable
fun PlantCard(plant: Plant) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // RECUADRO DE IMAGEN / PLACEHOLDER
            if (!plant.imagen_url.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(data = plant.imagen_url!!),
                    contentDescription = plant.nombre_comun,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp),
                    onFailure = {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.LightGray)
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin foto", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plant.nombre_comun,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = plant.categoria,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}