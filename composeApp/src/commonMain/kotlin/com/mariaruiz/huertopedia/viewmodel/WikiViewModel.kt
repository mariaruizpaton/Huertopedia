package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.repositories.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WikiViewModel : ViewModel() {

    // Lista completa (privada)
    private val allPlants = PlantRepository.listaDePlantas

    // Estado de la UI (pública): Lista filtrada que verá la pantalla
    private val _uiState = MutableStateFlow(allPlants)
    val plants = _uiState.asStateFlow()

    // Función para buscar/filtrar (puedes llamarla desde la UI)
    fun filterPlants(query: String, category: String) {
        _uiState.update {
            allPlants.filter { plant ->
                // Filtrado por nombre
                val matchesSearch = plant.name.contains(query, ignoreCase = true)
                // Filtrado por categoría (Si es "Todo", pasa siempre)
                val matchesCategory = if (category == "Todo") true else plant.category == category

                matchesSearch && matchesCategory
            }
        }
    }
}