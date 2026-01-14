package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

@Composable
fun WikiScreen(
    onLogout: () -> Unit,
    viewModel: LoginViewModel

) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todo") }

    // TODO: Replace with actual plant data and images
    val plants = listOf(
        Plant("Tomate", "Solanum lycopersicum"),
        Plant("Lechuga", "Lactuca sativa"),
        Plant("Fresa", "Fragaria"),
        Plant("Menta", "Mentha")
    )

    Scaffold(
        topBar = {
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
                    FilterChip("Todo", selectedFilter) { selectedFilter = "Todo" }
                    FilterChip("Hortalizas", selectedFilter) { selectedFilter = "Hortalizas" }
                    FilterChip("Frutas", selectedFilter) { selectedFilter = "Frutas" }
                    FilterChip("Hierbas", selectedFilter) { selectedFilter = "Hierbas" }
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(paddingValues),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    text: String,
    selectedFilter: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = text == selectedFilter,
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
fun PlantCard(plant: Plant) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Add actual image painter here
            /*Image(
                painter = plant.image,
                contentDescription = plant.name,
                modifier = Modifier.size(100.dp)
            )*/
            Text(plant.name)
            Text(plant.scientificName)
        }
    }
}

data class Plant(val name: String, val scientificName: String)