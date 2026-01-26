package com.mariaruiz.huertopedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariaruiz.huertopedia.model.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.mariaruiz.huertopedia.utils.getCurrentTimeMillis
import com.mariaruiz.huertopedia.utils.toFirebaseData
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class GardenViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val _availablePlants = MutableStateFlow<List<Plant>>(emptyList())
    val availablePlants = _availablePlants.asStateFlow()

    private val _planters = MutableStateFlow<List<Planter>>(emptyList())
    val planters = _planters.asStateFlow()

    val globalLastActivity: Flow<CropLog?> = planters.flatMapLatest { plantersList ->
        if (plantersList.isEmpty()) {
            flowOf(null)
        } else {
            // Creamos una lista de flujos (uno por cada jardinera)
            val logsFlows = plantersList.map { observeCropLogs(it.id) }

            // Combinamos todos los flujos en uno solo
            combine(logsFlows) { logsArray ->
                // logsArray contiene varias listas de logs. Las aplanamos en una sola lista.
                logsArray.flatMap { it }
                    // Ordenamos para obtener el más reciente por fecha (asumiendo que CropLog tiene un campo 'fecha')
                    .maxByOrNull { it.timestamp }
            }
        }
    }

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
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to nombre,
                    "filas" to filas,
                    "columnas" to columnas,
                    "fechaCreacion" to clockNow()
                )
                db.collection("usuario").document(uid).collection("planters").add(data)
            } catch (e: Exception) {}
        }
    }

    fun manageFlowerpots(planterId: String, positions: List<Pair<Int, Int>>, planta: Plant?, accion: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // OBTENER URL DE DESCARGA SI ES NECESARIO
                var urlPublica = planta?.imagenUrl
                if (urlPublica != null && !urlPublica.startsWith("http")) {
                    try {
                        urlPublica = storage.reference(urlPublica).getDownloadUrl()
                    } catch (e: Exception) {
                        println("Error obteniendo URL para maceta: ${e.message}")
                    }
                }

                positions.forEach { (f, c) ->
                    val potId = "r${f}_c${c}"
                    val ref = db.collection("usuario").document(uid)
                        .collection("planters").document(planterId)
                        .collection("flowerpots").document(potId)

                    if (accion == "Recolectar" || accion == "Arrancar") {
                        ref.delete()
                    } else {
                        val flowerpot = GardenFlowerpot(
                            id = potId, planterId = planterId,
                            fila = f, columna = c,
                            plantaId = planta?.id, nombrePlanta = planta?.nombreComun,
                            imagenUrl = urlPublica, // Guardamos la URL pública
                            fechaSiembra = clockNow(),
                            tipoAccion = accion
                        )
                        ref.set(flowerpot)
                    }
                }
            } catch (e: Exception) {}
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

    // --- FUNCIONES DEL DIARIO (CROP LOG) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCropLogs(planterId: String): Flow<List<CropLog>> {
        return auth.authStateChanged.flatMapLatest { user ->
            val uid = user?.uid
            if (uid != null && planterId.isNotBlank()) {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .collection("crop_log")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.data<CropLog>().copy(id = doc.id)
                            } catch (e: Exception) { null }
                        }
                    }
            } else flowOf(emptyList())
        }
    }

    fun addCropLogEntry(log: CropLog, imageBytes: ByteArray?) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                var finalLog = log
                if (imageBytes != null) {
                    val path = "logs/${uid}_${getCurrentTimeMillis()}.jpg"
                    val ref = storage.reference.child(path)
                    ref.putData(imageBytes.toFirebaseData())
                    val url = ref.getDownloadUrl()
                    finalLog = log.copy(photoPath = url)
                }
                db.collection("usuario").document(uid)
                    .collection("planters").document(log.planterId)
                    .collection("crop_log").add(finalLog)
            } catch (e: Exception) {}
        }
    }

    fun deleteCropLogEntry(planterId: String, logId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .collection("crop_logs").document(logId).delete()
            } catch (e: Exception) {}
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
                        recoleccion = doc.get<String>("recoleccion") ?: "",
                        temperaturaOptima = doc.get<String>("temperatura_optima") ?: "",
                        riego = doc.get<String>("riego") ?: "",
                        abono = doc.get<String>("abono") ?: "",
                        cuidados = doc.get<String>("cuidados") ?: "",
                        plantasAmigables = doc.get<List<String>>("plantas_amigables") ?: emptyList(),
                        plantasEnemigas = doc.get<List<String>>("plantas_enemigas") ?: emptyList()
                    )
                }
                _availablePlants.value = plants
            } catch (e: Exception) {}
        }
    }

    fun updatePlanterName(planterId: String, newName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .update("nombre" to newName)
            } catch (e: Exception) {}
        }
    }

    fun deletePlanter(planterId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId).delete()
            } catch (e: Exception) {}
        }
    }

    private fun clockNow(): Long = getCurrentTimeMillis()
}
