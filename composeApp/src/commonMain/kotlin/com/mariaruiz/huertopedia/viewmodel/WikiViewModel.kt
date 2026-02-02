package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.model.LocalizedText
import com.mariaruiz.huertopedia.model.Plant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de la enciclopedia de plantas (Wiki).
 *
 * Gestiona el estado y la lógica para obtener, mostrar y filtrar la lista de plantas.
 * Los datos se obtienen de Firestore y las imágenes de Firebase Storage.
 */
class WikiViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private var allPlants: List<Plant> = emptyList() // Caché con todas las plantas

    // Estado de la UI que expone la lista de plantas filtradas a mostrar
    private val _uiState = MutableStateFlow<List<Plant>>(emptyList())
    val plants = _uiState.asStateFlow()

    init {
        fetchPlants()
    }

    /**
     * Obtiene la lista completa de plantas desde Firestore y resuelve las URLs de sus imágenes.
     *
     * 1.  Carga la colección "plantas" de Firestore.
     * 2.  Mapea cada documento a un objeto `Plant`.
     * 3.  Para cada planta, si `imagenUrl` es una ruta de Storage, la convierte a una URL de descarga pública.
     * 4.  Actualiza la caché `allPlants` y el estado `_uiState` con la lista completa.
     * 5.  Gestiona y muestra en consola los errores que puedan ocurrir durante el proceso.
     */
    private fun fetchPlants() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("plantas").get()
                val plantsFromFirestore = snapshot.documents.mapNotNull { doc ->
                    try {
                        Plant(
                            id = doc.id,
                            nombreComun = doc.get<LocalizedText>("nombre_comun"),
                            nombreCientifico = doc.get<String>("nombre_cientifico") ?: "",
                            categoria = doc.get<LocalizedText>("categoria"), 
                            imagenUrl = doc.get<String>("imagen_url"),
                            siembra = doc.get<LocalizedText>("siembra"),
                            recoleccion = doc.get<LocalizedText>("recoleccion"),
                            temperaturaOptima = doc.get<String>("temperatura_optima") ?: "",
                            riego = doc.get<LocalizedText>("riego"),
                            abono = doc.get<LocalizedText>("abono"),
                            cuidados = doc.get<LocalizedText>("cuidados"),
                            plantasAmigables = doc.get<List<LocalizedText>>("plantas_amigables"),
                            plantasEnemigas = doc.get<List<LocalizedText>>("plantas_enemigas")
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

    /**
     * Filtra la lista de plantas basándose en una consulta de texto y una categoría.
     *
     * @param query El texto a buscar en el nombre común de la planta (ignora mayúsculas/minúsculas).
     * @param categoryValue La categoría por la que filtrar. Si es "Todo", no se aplica filtro de categoría.
     * @param langCode El código de idioma actual para realizar la búsqueda en el nombre y categoría correctos.
     */
    fun filterPlants(query: String, categoryValue: String, langCode: String) {
        _uiState.value = allPlants.filter { plant ->
            // 1. Filtrado por nombre en el idioma actual
            val matchesSearch = plant.nombreComun.get(langCode).contains(query, ignoreCase = true)
            
            // 2. Filtrado por categoría (soporta múltiples categorías separadas por '/')
            val matchesCategory = if (categoryValue == "Todo") {
                true
            } else {
                // Obtenemos las categorías (siempre comparamos contra el valor en español 
                // ya que es el ID interno que usamos en el WikiScreen para filtrar)
                val categoriesString = plant.categoria.get("es")
                // Separamos por la barra y comprobamos si alguna coincide
                categoriesString.split("/").any { it.equals(categoryValue, ignoreCase = true) }
            }
            
            matchesSearch && matchesCategory
        }
    }
}
