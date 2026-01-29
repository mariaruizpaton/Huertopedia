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

class GardenViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val _availablePlants = MutableStateFlow<List<Plant>>(emptyList())
    val availablePlants = _availablePlants.asStateFlow()

    private val _planters = MutableStateFlow<List<Planter>>(emptyList())
    val planters = _planters.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val globalLastActivity: Flow<CropLog?> = auth.authStateChanged.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            // SINTAXIS CORREGIDA: .where { ... } y nombre de colección 'crop_log'
            db.collectionGroup("crop_log")
                .where { "userId" equalTo uid }
                .orderBy("timestamp", dev.gitlive.firebase.firestore.Direction.DESCENDING)
                .limit(1)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.firstOrNull()?.let { doc ->
                        try { doc.data<CropLog>() } catch (e: Exception) { null }
                    }
                }
                .catch { emit(null) }
        } else {
            flowOf(null)
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
        // LIMITACIÓN DE TAMAÑO: Máximo 2 filas y 8 columnas
        val finalFilas = filas.coerceIn(1, 2)
        val finalColumnas = columnas.coerceIn(1, 8)
        
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to nombre,
                    "filas" to finalFilas,
                    "columnas" to finalColumnas,
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
                var urlPublica = planta?.imagenUrl
                if (urlPublica != null && !urlPublica.startsWith("http")) {
                    try {
                        urlPublica = storage.reference(urlPublica).getDownloadUrl()
                    } catch (e: Exception) { }
                }

                // Generamos el texto localizado para la acción (Guardado bilingüe)
                val localizedAction = if (accion == "Plantar") {
                    LocalizedText(es = "Plantar", en = "Plant")
                } else {
                    LocalizedText(es = "Sembrar", en = "Sow")
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
                            plantaId = planta?.id, 
                            nombrePlanta = planta?.nombreComun, // LocalizedText
                            imagenUrl = urlPublica, 
                            fechaSiembra = clockNow(),
                            tipoAccion = localizedAction 
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
                var finalLog = log.copy(userId = uid) 
                if (imageBytes != null) {
                    val path = "logs/${uid}_${getCurrentTimeMillis()}.jpg"
                    val ref = storage.reference.child(path)
                    ref.putData(imageBytes.toFirebaseData())
                    val url = ref.getDownloadUrl()
                    finalLog = finalLog.copy(photoPath = url)
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
                    .collection("crop_log").document(logId).delete()
            } catch (e: Exception) {}
        }
    }

    fun loadAvailablePlants() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("plantas").get()
                val plants = snapshot.documents.mapNotNull { doc ->
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
                    } catch (e: Exception) { null }
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
