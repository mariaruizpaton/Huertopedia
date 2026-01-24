package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.model.*
import com.mariaruiz.huertopedia.utils.getCurrentTimeMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GardenViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val _availablePlants = MutableStateFlow<List<Plant>>(emptyList())
    val availablePlants = _availablePlants.asStateFlow()

    private val _planters = MutableStateFlow<List<Planter>>(emptyList())
    val planters = _planters.asStateFlow()

    init {
        loadAvailablePlants()
        observePlanters()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlanters() {
        auth.authStateChanged
            .flatMapLatest { user ->
                val uid = user?.uid
                if (uid != null) {
                    db.collection("usuario").document(uid)
                        .collection("planters")
                        .snapshots()
                        .map { snapshot ->
                            snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.data<Planter>().copy(id = doc.id)
                                } catch (e: Exception) {
                                    println("Error mapeando: ${e.message}")
                                    null
                                }
                            }
                        }
                } else {
                    flowOf(emptyList())
                }
            }
            .onEach { _planters.value = it }
            .launchIn(viewModelScope)
    }

    fun createPlanter(nombre: String, filas: Int, columnas: Int) {
        val uid = auth.currentUser?.uid ?: return
        val validRows = filas.coerceIn(GardenConfig.MIN_ROWS, GardenConfig.MAX_ROWS)
        val validCols = columnas.coerceIn(GardenConfig.MIN_COLS, GardenConfig.MAX_COLS)

        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to nombre,
                    "filas" to validRows,
                    "columnas" to validCols,
                    "fechaCreacion" to clockNow()
                )
                db.collection("usuario").document(uid)
                    .collection("planters")
                    .add(data)
            } catch (e: Exception) {
                println("Error crear: ${e.message}")
            }
        }
    }

    fun manageFlowerpots(planterId: String, positions: List<Pair<Int, Int>>, planta: Plant?, accion: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                var urlPublica = planta?.imagenUrl
                if (urlPublica != null && !urlPublica.startsWith("http")) {
                    try {
                        urlPublica = storage.reference(urlPublica).getDownloadUrl()
                    } catch (e: Exception) {
                        println("Error URL: ${e.message}")
                    }
                }

                positions.forEach { (f, c) ->
                    val potId = "r${f}_c${c}"
                    val ref = db.collection("usuario").document(uid)
                        .collection("planters").document(planterId)
                        .collection("flowerpots").document(potId)

                    if (accion == "Recolectar") {
                        ref.delete()
                    } else {
                        val flowerpot = GardenFlowerpot(
                            id = potId, planterId = planterId,
                            fila = f, columna = c,
                            plantaId = planta?.id, nombrePlanta = planta?.nombreComun,
                            imagenUrl = urlPublica, fechaSiembra = clockNow(),
                            tipoAccion = accion
                        )
                        ref.set(flowerpot)
                    }
                }
            } catch (e: Exception) {
                println("Error macetas: ${e.message}")
            }
        }
    }

    fun getFlowerpots(planterId: String): Flow<List<GardenFlowerpot>> {
        return auth.authStateChanged.flatMapLatest { user ->
            val uid = user?.uid
            if (uid != null && planterId.isNotBlank()) {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .collection("flowerpots")
                    .snapshots()
                    .map { snapshot -> snapshot.documents.map { it.data<GardenFlowerpot>() } }
            } else {
                flowOf(emptyList())
            }
        }
    }
    fun observeCropLogs(planterId: String): Flow<List<CropLog>> {
        val uid = auth.currentUser?.uid
        if (uid == null || planterId.isBlank()) return flowOf(emptyList())

        return db.collection("usuario").document(uid)
            .collection("planters").document(planterId)
            .collection("crop_log")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    try {
                        it.data<CropLog>().copy(id = it.id)
                    } catch (e: Exception) {
                        println("Error al mapear CropLog: ${e.message}")
                        null
                    }
                }
            }
    }

    fun addCropLogEntry(log: CropLog) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val collectionRef = db.collection("usuario").document(uid)
                    .collection("planters").document(log.planterId)
                    .collection("crop_log")

                val newDocRef = collectionRef.add(log)
                
                collectionRef.document(newDocRef.id).update("id" to newDocRef.id)

            } catch (e: Exception) {
                println("Error al añadir entrada al diario: ${e.message}")
            }
        }
    }
    fun deleteCropLogEntry(planterId: String, logId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (planterId.isBlank() || logId.isBlank()) return

        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .collection("crop_log").document(logId)
                    .delete()
            } catch (e: Exception) {
                println("Error al borrar la entrada del diario: ${e.message}")
            }
        }
    }


    fun loadAvailablePlants() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("plantas").get()
                val plants = snapshot.documents.map { doc ->
                    Plant(
                        id = doc.id,
                        nombreComun = doc.get<String>("nombre_comun") ?: "",
                        nombreCientifico = doc.get<String>("nombre_cientifico") ?: "",
                        categoria = doc.get<String>("categoria") ?: "",
                        imagenUrl = doc.get<String>("imagen_url"),
                        siembra = doc.get<String>("siembra") ?: "",
                        recoleccion = doc.get<String>("recoleccion") ?: "", // CORREGIDO: Sin tilde
                        temperaturaOptima = doc.get<String>("temperatura_optima") ?: "",
                        riego = doc.get<String>("riego") ?: "",
                        abono = doc.get<String>("abono") ?: "",
                        cuidados = doc.get<String>("cuidados") ?: "",
                        plantasAmigables = doc.get<List<String>>("plantas_amigables") ?: emptyList(),
                        plantasEnemigas = doc.get<List<String>>("plantas_enemigas") ?: emptyList()
                    )
                }
                _availablePlants.value = plants
            } catch (e: Exception) {
                println("Error catálogo huerto: ${e.message}")
            }
        }
    }

    fun updatePlanterName(planterId: String, newName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .update("nombre" to newName)
            } catch (e: Exception) {
                println("Error al actualizar nombre: ${e.message}")
            }
        }
    }

    fun deletePlanter(planterId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (planterId.isBlank()) return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .delete()
            } catch (e: Exception) {
                println("Error al borrar: ${e.message}")
            }
        }
    }

    private fun clockNow(): Long = getCurrentTimeMillis()
}
