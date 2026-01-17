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

    // Usamos las librerías de GitLive (compatibles con iOS/Android)
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
                // 1. Descargar documentos de Firestore
                val snapshot = db.collection("plantas").get()

                // 2. Convertir documentos a objetos Plant (CAMBIO AQUÍ: MANUALMENTE)
                // Usamos mapNotNull para que si una falla, no rompa las demás
                val plantsFromFirestore = snapshot.documents.mapNotNull { doc ->
                    try {
                        Plant(
                            id = doc.id,
                            // Leemos campo a campo. Si falla uno, salta al catch y te avisa en el Log
                            nombre_comun = doc.get<String>("nombre_comun"),
                            categoria = doc.get<String>("categoria"),
                            // Para campos opcionales o listas, protegemos con try-catch individual
                            imagen_url = try { doc.get<String>("imagen_url") } catch (e: Exception) { null },
                            dias_cosecha = try { doc.get<Int>("dias_cosecha") } catch (e: Exception) { 0 },
                            frecuencia_riego = try { doc.get<String>("frecuencia_riego") } catch (e: Exception) { "" },
                            tipo_sustrato = try { doc.get<String>("tipo_sustrato") } catch (e: Exception) { "" },
                            plantas_amigables = try { doc.get<List<String>>("plantas_amigables") } catch (e: Exception) { emptyList() },
                            plantas_enemigas = try { doc.get<List<String>>("plantas_enemigas") } catch (e: Exception) { emptyList() },
                            temporada_siembra = try { doc.get<List<String>>("temporada_siembra") } catch (e: Exception) { emptyList() }
                        )
                    } catch (e: Exception) {
                        println("ERROR al leer planta con ID ${doc.id}: ${e.message}")
                        null // Esta planta se ignora, pero el resto se mostrará
                    }
                }

                // 3. Procesar imágenes en paralelo (ESTO SE QUEDA IGUAL)
                val plantsWithImageUrls = plantsFromFirestore.map { plant ->
                    async {
                        plant.imagen_url?.let { path ->
                            if (path.startsWith("http")) return@async plant

                            try {
                                val imageRef = storage.reference(path)
                                val downloadUrl = imageRef.getDownloadUrl()
                                plant.copy(imagen_url = downloadUrl)
                            } catch (e: Exception) {
                                println("Error obteniendo imagen para $path: ${e.message}")
                                plant.copy(imagen_url = null)
                            }
                        } ?: plant
                    }
                }.awaitAll()

                // 4. Actualizamos la UI
                allPlants = plantsWithImageUrls
                _uiState.value = allPlants

                // Un log para que sepas cuantas ha bajado
                println("WikiViewModel: Se han descargado ${allPlants.size} plantas.")

            } catch (e: Exception) {
                println("Error GENERAL al obtener las plantas: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun filterPlants(query: String, category: String) {
        _uiState.value = allPlants.filter { plant ->
            val matchesSearch = plant.nombre_comun.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "Todo") true else plant.categoria.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    fun setPlants(newList: List<Plant>) {
        allPlants = newList
        _uiState.value = newList
    }
}