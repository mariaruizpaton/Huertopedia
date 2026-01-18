package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.model.Plant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WikiViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private var allPlants: List<Plant> = emptyList()

    private val _uiState = MutableStateFlow<List<Plant>>(emptyList())
    val plants = _uiState.asStateFlow()

    init {
        fetchPlants()
    }

    private fun fetchPlants() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("plantas").get()
                val plantsFromFirestore = snapshot.documents.mapNotNull { doc ->
                    try {
                        Plant(
                            id = doc.id,
                            nombreComun = doc.get<String>("nombre_comun"),
                            categoria = doc.get<String>("categoria"),
                            imagenUrl = try { doc.get<String>("imagen_url") } catch (_: Exception) { null },
                            diasCosecha = try { doc.get<Int>("dias_cosecha") } catch (_: Exception) { 0 },
                            frecuenciaRiego = try { doc.get<String>("frecuencia_riego") } catch (_: Exception) { "" },
                            tipoSustrato = try { doc.get<String>("tipo_sustrato") } catch (_: Exception) { "" },
                            plantasAmigables = try { doc.get<List<String>>("plantas_amigables") } catch (_: Exception) { emptyList() },
                            plantasEnemigas = try { doc.get<List<String>>("plantas_enemigas") } catch (_: Exception) { emptyList() },
                            temporadaSiembra = try { doc.get<List<String>>("temporada_siembra") } catch (_: Exception) { emptyList() }
                        )
                    } catch (e: Exception) {
                        println("ERROR al leer planta con ID ${doc.id}: ${e.message}")
                        null
                    }
                }

                val plantsWithImageUrls = plantsFromFirestore.map { plant ->
                    async {
                        plant.imagenUrl?.let { path ->
                            if (path.startsWith("http")) return@async plant
                            try {
                                val imageRef = storage.reference(path)
                                val downloadUrl = imageRef.getDownloadUrl()
                                plant.copy(imagenUrl = downloadUrl)
                            } catch (e: Exception) {
                                println("Error obteniendo imagen para $path: ${e.message}")
                                plant.copy(imagenUrl = null)
                            }
                        } ?: plant
                    }
                }.awaitAll()

                allPlants = plantsWithImageUrls
                _uiState.value = allPlants

            } catch (e: Exception) {
                println("Error GENERAL al obtener las plantas: ${e.message}")
            }
        }
    }

    fun filterPlants(query: String, category: String) {
        _uiState.value = allPlants.filter { plant ->
            val matchesSearch = plant.nombreComun.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Todo") true else plant.categoria.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    /* iOS
    fun setPlants(newList: List<Plant>) {
        allPlants = newList
        _uiState.value = newList
    }

    fun updatePlantImageUrl(id: String, url: String) {
        val updateFunc: (Plant) -> Plant = { plant ->
            if (plant.id == id) plant.copy(imagenUrl = url) else plant
        }
        allPlants = allPlants.map(updateFunc)
        _uiState.update { it.map(updateFunc) }
    }
    */
}
