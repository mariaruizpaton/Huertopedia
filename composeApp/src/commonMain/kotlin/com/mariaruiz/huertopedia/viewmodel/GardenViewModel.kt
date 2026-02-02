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

/**
 * ViewModel para gestionar el estado y las interacciones relacionadas con el "Huerto" del usuario.
 *
 * Se encarga de:
 * - Observar y gestionar las jardineras (`Planter`) del usuario.
 * - Crear, modificar y eliminar jardineras.
 * - Gestionar las macetas (`GardenFlowerpot`) dentro de cada jardinera (plantar, recolectar, etc.).
 * - Observar y gestionar el diario de cultivo (`CropLog`).
 * - Cargar la lista de plantas disponibles para plantar.
 */
class GardenViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    // --- Flujos de Estado para la UI ---
    private val _availablePlants = MutableStateFlow<List<Plant>>(emptyList())
    val availablePlants = _availablePlants.asStateFlow()

    private val _planters = MutableStateFlow<List<Planter>>(emptyList())
    val planters = _planters.asStateFlow()

    /**
     * Flujo que emite la última entrada registrada en el diario de cultivo global del usuario.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val globalLastActivity: Flow<CropLog?> = auth.authStateChanged.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
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

    /**
     * Se suscribe a los cambios en la colección de jardineras del usuario actual y actualiza el estado `planters`.
     */
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

    /**
     * Crea una nueva jardinera en Firestore para el usuario actual.
     * @param nombre El nombre de la jardinera.
     * @param filas El número de filas (limitado a un máximo de 2).
     * @param columnas El número de columnas (limitado a un máximo de 8).
     */
    fun createPlanter(nombre: String, filas: Int, columnas: Int) {
        val uid = auth.currentUser?.uid ?: return
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

    /**
     * Realiza una acción (plantar, sembrar, recolectar) sobre una o varias macetas.
     * @param planterId El ID de la jardinera.
     * @param positions La lista de posiciones (fila, columna) de las macetas afectadas.
     * @param planta La planta a sembrar/plantar (null si la acción es recolectar/arrancar).
     * @param accion La acción a realizar ("Plantar", "Sembrar", "Recolectar", "Arrancar").
     */
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
                            nombrePlanta = planta?.nombreComun, 
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

    /**
     * Obtiene un flujo con la lista de macetas para una jardinera específica.
     * @param planterId El ID de la jardinera.
     * @return Un `Flow` que emite la lista de `GardenFlowerpot`.
     */
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

    /**
     * Obtiene un flujo con las entradas del diario de cultivo para una jardinera específica.
     * @param planterId El ID de la jardinera.
     * @return Un `Flow` que emite la lista de `CropLog`.
     */
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

    /**
     * Añade una nueva entrada al diario de cultivo.
     * @param log El objeto `CropLog` a añadir.
     * @param imageBytes Un `ByteArray` opcional con una imagen para adjuntar a la entrada.
     */
    fun addCropLogEntry(log: CropLog, imageBytes: ByteArray?) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                var finalLog = log.copy(userId = uid) 
                if (imageBytes != null) {
                    val imagePath = "crop_logs/${log.planterId}/${log.timestamp}.jpg"
                    val ref = storage.reference.child(imagePath)
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

    /**
     * Elimina una entrada del diario de cultivo y su foto asociada si existe.
     * @param planterId El ID de la jardinera.
     * @param logId El ID de la entrada a eliminar.
     * @param photoUrl La URL de la foto asociada (si tiene).
     */
    fun deleteCropLogEntry(planterId: String, logId: String, photoUrl: String?) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Borrar el documento de Firestore (Base de datos)
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId)
                    .collection("crop_log").document(logId).delete()

                // 2. Borrar la imagen de Storage (si existe URL)
                if (!photoUrl.isNullOrBlank()) {
                    try {
                        val path = photoUrl.substringAfter("/o/").substringBefore("?").replace("%2F", "/")
                        storage.reference(path).delete()
                    } catch (e: Exception) {
                        println("Error al borrar la imagen: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Error al borrar la entrada: ${e.message}")
            }
        }
    }

    /**
     * Carga la lista completa de plantas disponibles desde la colección "plantas" de Firestore.
     */
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

    /**
     * Actualiza el nombre de una jardinera.
     * @param planterId El ID de la jardinera a actualizar.
     * @param newName El nuevo nombre para la jardinera.
     */
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

    /**
     * Elimina una jardinera y todo su contenido (macetas, diario de cultivo, etc.).
     * @param planterId El ID de la jardinera a eliminar.
     */
    fun deletePlanter(planterId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuario").document(uid)
                    .collection("planters").document(planterId).delete()
            } catch (e: Exception) {}
        }
    }

    /**
     * Devuelve la marca de tiempo actual en milisegundos.
     * @return El tiempo actual en milisegundos.
     */
    private fun clockNow(): Long = getCurrentTimeMillis()
}
