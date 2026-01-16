package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.model.Plant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.FirebaseStorage
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
                val plantsFromFirestore = snapshot.documents.map { doc ->
                    val plant = doc.data<Plant>()
                    plant.copy(id = doc.id)
                }
                val plantsWithImageUrls = plantsFromFirestore.map { plant ->
                    async {        plant.imagen_url?.let { relativePath ->
                        try {
                            val imageRef = storage.reference(relativePath)

                            val downloadUrl = imageRef.getDownloadUrl()
                            plant.copy(imagen_url = downloadUrl)

                        } catch (e: Exception) {
                            println("Error al obtener URL de descarga para $relativePath: ${e.message}")
                            plant.copy(imagen_url = null)
                        }
                    } ?: plant
                    }
                }.awaitAll()
                allPlants = plantsWithImageUrls
                _uiState.value = allPlants
            } catch (e: Exception) {
                println("Error al obtener las plantas: ${e.message}")
            }
        }
    }

    fun filterPlants(query: String, category: String) {
        _uiState.value = allPlants.filter { plant ->
            val matchesSearch = plant.nombre_comun.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Todo") true else plant.categoria == category.lowercase()
            matchesSearch && matchesCategory
        }
    }
}
