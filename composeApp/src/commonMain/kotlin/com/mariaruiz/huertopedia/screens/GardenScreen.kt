package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mariaruiz.huertopedia.model.GardenConfig
import com.mariaruiz.huertopedia.model.GardenFlowerpot
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
// Importamos nuestro helper de compartir multiplataforma
import com.mariaruiz.huertopedia.utils.rememberShareHandler
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GardenScreen(
    onBack: () -> Unit,
    viewModel: LoginViewModel,
    gardenViewModel: GardenViewModel,
    onNavigateToLog: (Planter) -> Unit
) {
    val strings = LocalStrings.current
    val myPlanters by gardenViewModel.planters.collectAsState(initial = emptyList())
    val availablePlants by gardenViewModel.availablePlants.collectAsState()

    // Estados
    var showCreateDialog by remember { mutableStateOf(false) }
    var nombrePlanter by remember { mutableStateOf("") }
    var numFilas by remember { mutableIntStateOf(1) }
    var numColumnas by remember { mutableIntStateOf(1) }

    // Estados para edición
    var showEditDialog by remember { mutableStateOf(false) }
    var planterToEdit by remember { mutableStateOf<Planter?>(null) }
    var nuevoNombrePlanter by remember { mutableStateOf("") }

    // Selección múltiple
    var selectedPots by remember { mutableStateOf(emptyMap<Triple<String, Int, Int>, Boolean>()) }

    // Estados de Error
    var showSelectionError by remember { mutableStateOf(false) }
    var showMultiplePlantersError by remember { mutableStateOf(false) }

    // Conflicto de Enemigas
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessage by remember { mutableStateOf("") }

    // Confirmación de Recolección / Arrancado
    var showConfirmRemovalDialog by remember { mutableStateOf(false) }

    var showPlantDialog by remember { mutableStateOf(false) }
    var tipoAccionSeleccionada by remember { mutableStateOf("Plantar") }
    var selectedPlantForPot by remember { mutableStateOf<Plant?>(null) }
    var expandedPlantMenu by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var planterToDelete by remember { mutableStateOf<Planter?>(null) }

    // Estado para la animación
    var showHarvestAnimation by remember { mutableStateOf(false) }

    val isHarvestMode = selectedPots.values.any { it }

    LaunchedEffect(isHarvestMode) {
        tipoAccionSeleccionada = if (isHarvestMode) "Recolectar" else "Plantar"
    }

    // Efecto para detener la animación automáticamente
    if (showHarvestAnimation) {
        LaunchedEffect(Unit) {
            delay(2000)
            showHarvestAnimation = false
        }
    }

    BackHandler { onBack() }

    // Función auxiliar para conflictos
    fun checkConflicts(planterId: String, selectedPlant: Plant, positions: List<Pair<Int, Int>>, currentOccupied: List<GardenFlowerpot>): String? {
        positions.forEach { (f, c) ->
            val neighbors = listOf(f-1 to c, f+1 to c, f to c-1, f to c+1)
            neighbors.forEach { (nf, nc) ->
                val neighborPot = currentOccupied.find { it.fila == nf && it.columna == nc }
                neighborPot?.nombrePlanta?.let { neighborName ->
                    val neighborPlantData = availablePlants.find { it.nombreComun == neighborName }
                    val isEnemyOfNew = selectedPlant.plantasEnemigas.any { it.equals(neighborName, ignoreCase = true) }
                    val isNewEnemyOfNeighbor = neighborPlantData?.plantasEnemigas?.any { it.equals(selectedPlant.nombreComun, ignoreCase = true) } ?: false
                    if (isEnemyOfNew || isNewEnemyOfNeighbor) {
                        return strings.gardenConflictText.replace("{0}", selectedPlant.nombreComun).replace("{1}", neighborName)
                    }
                }
            }
        }
        return null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.gardenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (selectedPots.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showPlantDialog = true },
                        icon = { Icon(Icons.Default.Agriculture, null) },
                        text = { Text(strings.gardenActivityButton.replace("{0}", selectedPots.size.toString())) },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = strings.gardenAddPlanter)
                }
            }
        }
    ) { padding ->

        // Box principal para capas superpuestas (animación)
        Box(modifier = Modifier.fillMaxSize()) {

            // --- CONTENIDO PRINCIPAL ---
            if (myPlanters.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { Text(strings.gardenNoPlanters) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myPlanters) { planter ->
                        PlanterCard(
                            planter = planter,
                            gardenViewModel = gardenViewModel,
                            selectedPots = selectedPots.keys,
                            onDelete = { planterToDelete = planter; showDeleteConfirm = true },
                            onEditName = { planterToEdit = planter; nuevoNombrePlanter = planter.nombre; showEditDialog = true },
                            onPotClick = { f, c, isOccupied ->
                                val triple = Triple(planter.id, f, c)
                                if (selectedPots.containsKey(triple)) { selectedPots = selectedPots - triple }
                                else {
                                    if (selectedPots.isNotEmpty()) {
                                        val firstId = selectedPots.keys.first().first
                                        if (firstId != planter.id) { showMultiplePlantersError = true; return@PlanterCard }
                                        if (selectedPots.values.first() != isOccupied) { showSelectionError = true; return@PlanterCard }
                                    }
                                    selectedPots = selectedPots + (triple to isOccupied)
                                }
                            },
                            onLogClick = { onNavigateToLog(planter) }
                        )
                    }
                }
            }

            // --- ANIMACIÓN DE RECOLECCIÓN (Overlay) ---
            if (showHarvestAnimation) {
                HarvestAnimationOverlay()
            }
        }

        // --- DIÁLOGOS ---

        // Confirmación de eliminación (Cosecha/Arrancar)
        if (showConfirmRemovalDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmRemovalDialog = false },
                icon = {
                    Icon(
                        imageVector = if(tipoAccionSeleccionada == "Recolectar") Icons.Default.ShoppingBasket else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if(tipoAccionSeleccionada == "Recolectar") Color(0xFF4CAF50) else Color.Red
                    )
                },
                title = { Text(text = if(tipoAccionSeleccionada == "Recolectar") strings.gardenActionHarvest else strings.gardenActionPullOut, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = { Text(text = if(tipoAccionSeleccionada == "Recolectar") strings.gardenConfirmRemovalHarvest else strings.gardenConfirmRemovalPullOut, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showConfirmRemovalDialog = false }) { Text(strings.gardenNo) }
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = {
                            val planterId = selectedPots.keys.first().first
                            val positions = selectedPots.keys.map { it.second to it.third }
                            gardenViewModel.manageFlowerpots(planterId, positions, null, tipoAccionSeleccionada)

                            if (tipoAccionSeleccionada == "Recolectar") {
                                showHarvestAnimation = true
                            }

                            showConfirmRemovalDialog = false
                            showPlantDialog = false
                            selectedPots = emptyMap()
                        }) { Text(strings.gardenYes) }
                    }
                }
            )
        }

        // Conflicto plantas enemigas
        if (showConflictDialog) {
            AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800)) },
                title = { Text(text = strings.gardenConflictTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = { Text(text = conflictMessage, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showConflictDialog = false }) { Text(strings.gardenNo) }
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = {
                            val planterId = selectedPots.keys.first().first
                            val positions = selectedPots.keys.map { it.second to it.third }
                            gardenViewModel.manageFlowerpots(planterId, positions, selectedPlantForPot, tipoAccionSeleccionada)
                            showConflictDialog = false
                            showPlantDialog = false
                            selectedPots = emptyMap()
                        }) { Text(strings.gardenYes) }
                    }
                }
            )
        }

        // Errores de selección
        if (showMultiplePlantersError) {
            Dialog(onDismissRequest = { showMultiplePlantersError = false; selectedPots = emptyMap() }) {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.gardenMultiplePlantersErrorTitle, fontWeight = FontWeight.Bold)
                        Text(strings.gardenMultiplePlantersErrorText)
                        TextButton(onClick = { showMultiplePlantersError = false; selectedPots = emptyMap() }) { Text(strings.gardenOk) }
                    }
                }
            }
        }

        if (showSelectionError) {
            Dialog(onDismissRequest = { showSelectionError = false; selectedPots = emptyMap() }) {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.gardenSelectionErrorTitle, fontWeight = FontWeight.Bold)
                        Text(strings.gardenSelectionErrorText)
                        TextButton(onClick = { showSelectionError = false; selectedPots = emptyMap() }) { Text(strings.gardenOk) }
                    }
                }
            }
        }

        // Diálogo de Actividad (Plantar/Sembrar/Recolectar)
        if (showPlantDialog) {
            val planterId = selectedPots.keys.first().first
            val currentOccupiedPots by gardenViewModel.getFlowerpots(planterId).collectAsState(initial = emptyList())
            AlertDialog(
                onDismissRequest = { showPlantDialog = false },
                title = { Text(strings.gardenManagePots) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.gardenManagePotsQuestion.replace("{0}", selectedPots.size.toString()), textAlign = TextAlign.Center)
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                            if (isHarvestMode) {
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Recolectar",
                                    onClick = { tipoAccionSeleccionada = "Recolectar" },
                                    label = { Text(strings.gardenActionHarvest) },
                                    leadingIcon = { Icon(Icons.Default.ShoppingBasket, null, tint = if(tipoAccionSeleccionada=="Recolectar") Color(0xFF4CAF50) else Color.Gray) }
                                )
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Arrancar",
                                    onClick = { tipoAccionSeleccionada = "Arrancar" },
                                    label = { Text(strings.gardenActionPullOut) },
                                    leadingIcon = { Icon(Icons.Default.Warning, null, tint = if(tipoAccionSeleccionada=="Arrancar") Color.Red else Color.Gray) }
                                )
                            } else {
                                listOf("Plantar", "Sembrar").forEach { action ->
                                    FilterChip(
                                        selected = tipoAccionSeleccionada == action,
                                        onClick = { tipoAccionSeleccionada = action },
                                        label = { Text(if(action=="Plantar") strings.gardenActionPlant else strings.gardenActionSow) }
                                    )
                                }
                            }
                        }
                        if (!isHarvestMode) {
                            Box {
                                OutlinedTextField(
                                    value = selectedPlantForPot?.nombreComun ?: strings.gardenSelectPlant,
                                    onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { expandedPlantMenu = true },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline)
                                )
                                Box(Modifier.matchParentSize().clickable { expandedPlantMenu = true })
                                DropdownMenu(expanded = expandedPlantMenu, onDismissRequest = { expandedPlantMenu = false }) {
                                    availablePlants.forEach { plant ->
                                        DropdownMenuItem(text = { Text(plant.nombreComun) }, onClick = { selectedPlantForPot = plant; expandedPlantMenu = false })
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showPlantDialog = false }) { Text(strings.gardenCancel) }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            enabled = isHarvestMode || selectedPlantForPot != null,
                            onClick = {
                                val positions = selectedPots.keys.map { it.second to it.third }
                                if (isHarvestMode) {
                                    showConfirmRemovalDialog = true
                                    return@Button
                                }
                                if (selectedPlantForPot != null) {
                                    val conflict = checkConflicts(planterId, selectedPlantForPot!!, positions, currentOccupiedPots)
                                    if (conflict != null) {
                                        conflictMessage = conflict
                                        showConflictDialog = true
                                        return@Button
                                    }
                                }
                                gardenViewModel.manageFlowerpots(planterId, positions, selectedPlantForPot, tipoAccionSeleccionada)
                                showPlantDialog = false
                                selectedPots = emptyMap()
                            }
                        ) { Text(strings.gardenConfirm) }
                    }
                }
            )
        }

        // Crear Jardinera
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text(strings.gardenNewPlanter) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(value = nombrePlanter, onValueChange = { nombrePlanter = it }, label = { Text(strings.gardenPlanterNamePlaceholder) }, modifier = Modifier.fillMaxWidth())
                        NumberSelector(strings.gardenRows, numFilas, { numFilas = it }, GardenConfig.MIN_ROWS..GardenConfig.MAX_ROWS)
                        NumberSelector(strings.gardenCols, numColumnas, { numColumnas = it }, GardenConfig.MIN_COLS..GardenConfig.MAX_COLS)
                    }
                },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showCreateDialog = false }) { Text(strings.gardenCancel) }
                        Spacer(Modifier.width(16.dp))
                        Button(enabled = nombrePlanter.isNotBlank(), onClick = { gardenViewModel.createPlanter(nombrePlanter, numFilas, numColumnas); showCreateDialog = false; nombrePlanter = "" }) { Text(strings.gardenCreate) }
                    }
                }
            )
        }

        // Editar Nombre
        if (showEditDialog && planterToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text(strings.gardenEditNameTitle) },
                text = { OutlinedTextField(value = nuevoNombrePlanter, onValueChange = { nuevoNombrePlanter = it }, label = { Text(strings.gardenNewNameLabel) }, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showEditDialog = false }) { Text(strings.gardenCancel) }
                        Spacer(Modifier.width(16.dp))
                        Button(enabled = nuevoNombrePlanter.isNotBlank(), onClick = { planterToEdit?.id?.let { gardenViewModel.updatePlanterName(it, nuevoNombrePlanter) }; showEditDialog = false }) { Text(strings.gardenSave) }
                    }
                }
            )
        }

        // Confirmar Borrado de Jardinera
        if (showDeleteConfirm && planterToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(strings.gardenDelete) },
                text = { Text(text = strings.gardenDeleteConfirmQuestion.replace("{0}", planterToDelete?.nombre ?: ""), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.gardenCancel) }
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = { planterToDelete?.id?.let { gardenViewModel.deletePlanter(it) }; showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(strings.gardenDelete) }
                    }
                }
            )
        }
    }
}

@Composable
fun PlanterCard(
    planter: Planter,
    gardenViewModel: GardenViewModel,
    selectedPots: Set<Triple<String, Int, Int>>,
    onDelete: () -> Unit,
    onEditName: () -> Unit,
    onPotClick: (Int, Int, Boolean) -> Unit,
    onLogClick: () -> Unit
) {
    val strings = LocalStrings.current

    // Handler de compartir multiplataforma
    val shareHandler = rememberShareHandler()

    val occupiedPots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                // Nombre y Edición
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(planter.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onEditName, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp), MaterialTheme.colorScheme.outline) }
                }

                // Compartir y Borrar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        // --- CAMBIO AQUÍ: Generamos el link ---
                        // Usamos un esquema web estándar. Para que abra tu app en el futuro,
                        // necesitarás configurar "App Links" en el Manifest de Android.
                        val deepLinkUrl = "https://huertopedia.app/jardinera/${planter.id}"

                        val text = "🌱 ¡Mira mi progreso en Huertopedia! \n\n" +
                                "Estoy cuidando la jardinera '${planter.nombre}' con ${occupiedPots.size} plantas. 🌻\n\n" +
                                "Ver aquí: $deepLinkUrl"

                        shareHandler(text)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            // ... El resto de la tarjeta sigue igual ...
            Text(strings.gardenPlanterSize.replace("{0}", planter.filas.toString()).replace("{1}", planter.columnas.toString()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.widthIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (f in 0 until planter.filas) {
                        Row(
                            modifier = if (planter.columnas > 2) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            for (c in 0 until planter.columnas) {
                                val pot = occupiedPots.find { it.fila == f && it.columna == c }
                                val itemModifier = if (planter.columnas > 2) Modifier.weight(1f) else Modifier.size(100.dp)
                                FlowerpotView(pot, selectedPots.contains(Triple(planter.id, f, c)), itemModifier) { onPotClick(f, c, pot != null) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onLogClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, null)
                Spacer(Modifier.width(8.dp))
                Text(strings.gardenCropLog)
            }
        }
    }
}

@Composable
fun FlowerpotView(pot: GardenFlowerpot?, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = when {
        isSelected -> Color(0xFFFFF176)
        pot == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        pot.tipoAccion == "Sembrar" -> Color(0xFFE1BEE7)
        else -> Color(0xFFC8E6C9)
    }
    Box(modifier.aspectRatio(1f).background(bgColor, RoundedCornerShape(8.dp)).border(1.dp, if(isSelected) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).clickable { onClick() }, Alignment.Center) {
        if (pot == null) Icon(if(isSelected) Icons.Default.Check else Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        else {
            if (!pot.imagenUrl.isNullOrBlank()) KamelImage(asyncPainterResource(pot.imagenUrl!!), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(4.dp)))
            else Text(pot.nombrePlanta?.take(8) ?: "?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun NumberSelector(label: String, value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Default.Remove, null) }
            Text(value.toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Default.Add, null) }
        }
    }
}

// --- ANIMACIÓN DE HOJAS FLOTANTES (MULTIPLATFORM) ---
@Composable
fun HarvestAnimationOverlay() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerHeight = maxHeight
        val containerWidth = maxWidth

        val particles = remember {
            List(15) {
                LeafParticleData(
                    startXRatio = Random.nextFloat(),
                    delay = Random.nextLong(0, 500),
                    duration = Random.nextInt(1000, 2000),
                    icon = if (Random.nextBoolean()) Icons.Default.Eco else Icons.Default.Spa
                )
            }
        }

        particles.forEach { particle ->
            LeafParticle(particle, containerWidth, containerHeight)
        }
    }
}

data class LeafParticleData(
    val startXRatio: Float,
    val delay: Long,
    val duration: Int,
    val icon: ImageVector
)

// Extensión de BoxScope para poder usar .align()
@Composable
fun BoxScope.LeafParticle(
    data: LeafParticleData,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(data.delay)
        launch {
            offsetY.animateTo(
                targetValue = -containerHeight.value * 0.6f,
                animationSpec = tween(durationMillis = data.duration, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            delay(data.duration / 2L)
            alpha.animateTo(0f, animationSpec = tween(durationMillis = data.duration / 2))
        }
    }

    Icon(
        imageVector = data.icon,
        contentDescription = null,
        tint = Color(0xFF4CAF50),
        modifier = Modifier
            .offset(
                x = (containerWidth * data.startXRatio) - 24.dp,
                y = offsetY.value.dp
            )
            .align(Alignment.BottomStart)
            .alpha(alpha.value)
            .size(32.dp)
    )
}