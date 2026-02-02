package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.*
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
import com.mariaruiz.huertopedia.model.GardenFlowerpot
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import com.mariaruiz.huertopedia.utils.rememberShareHandler
import com.mariaruiz.huertopedia.utils.rememberVibrationHandler
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Muestra la pantalla principal del jardín del usuario, donde puede ver y gestionar sus jardineras.
 *
 * @param onBack Llama a esta función para navegar hacia atrás.
 * @param viewModel El [LoginViewModel] para gestionar el estado de la sesión.
 * @param gardenViewModel El [GardenViewModel] para gestionar los datos del jardín.
 * @param languageRepository El repositorio para obtener el idioma actual.
 * @param onNavigateToLog Llama a esta función para navegar al registro de una jardinera.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GardenScreen(
    onBack: () -> Unit,
    viewModel: LoginViewModel,
    gardenViewModel: GardenViewModel,
    languageRepository: LanguageRepository,
    onNavigateToLog: (Planter) -> Unit
) {
    val strings = LocalStrings.current
    val langCode by languageRepository.currentLanguage.collectAsState()
    val myPlanters by gardenViewModel.planters.collectAsState(initial = emptyList())
    val availablePlants by gardenViewModel.availablePlants.collectAsState()

    val vibrationHandler = rememberVibrationHandler()

    var showCreateDialog by remember { mutableStateOf(false) }
    var nombrePlanter by remember { mutableStateOf("") }
    var numFilas by remember { mutableIntStateOf(1) }
    var numColumnas by remember { mutableIntStateOf(1) }

    var showEditDialog by remember { mutableStateOf(false) }
    var planterToEdit by remember { mutableStateOf<Planter?>(null) }
    var nuevoNombrePlanter by remember { mutableStateOf("") }

    var selectedPots by remember { mutableStateOf(emptyMap<Triple<String, Int, Int>, Boolean>()) }
    var showSelectionError by remember { mutableStateOf(false) }
    var showMultiplePlantersError by remember { mutableStateOf(false) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessage by remember { mutableStateOf("") }
    var showConfirmRemovalDialog by remember { mutableStateOf(false) }

    var showPlantDialog by remember { mutableStateOf(false) }
    var tipoAccionSeleccionada by remember { mutableStateOf("Plantar") }
    var selectedPlantForPot by remember { mutableStateOf<Plant?>(null) }
    var expandedPlantMenu by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var planterToDelete by remember { mutableStateOf<Planter?>(null) }

    var showHarvestAnimation by remember { mutableStateOf(false) }

    val isHarvestMode = selectedPots.values.any { it }

    LaunchedEffect(isHarvestMode) {
        tipoAccionSeleccionada = if (isHarvestMode) "Recolectar" else "Plantar"
    }

    if (showHarvestAnimation) {
        LaunchedEffect(Unit) {
            delay(2000)
            showHarvestAnimation = false
        }
    }

    BackHandler { onBack() }

    fun checkConflicts(selectedPlant: Plant, positions: List<Pair<Int, Int>>, currentOccupied: List<GardenFlowerpot>): String? {
        positions.forEach { (f, c) ->
            val neighbors = listOf(f-1 to c, f+1 to c, f to c-1, f to c+1)
            neighbors.forEach { (nf, nc) ->
                val neighborPot = currentOccupied.find { it.fila == nf && it.columna == nc }
                neighborPot?.nombrePlanta?.let { neighborName ->
                    val isEnemyOfNew = selectedPlant.plantasEnemigas.any { it == neighborName }
                    if (isEnemyOfNew) {
                        return strings.gardenConflictText
                            .replace("{0}", selectedPlant.nombreComun.get(langCode))
                            .replace("{1}", neighborName.get(langCode))
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack) } }
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
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Default.Add, contentDescription = strings.gardenAddPlanter) }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (myPlanters.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Text(strings.gardenNoPlanters, color = MaterialTheme.colorScheme.onBackground) }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(myPlanters) { planter ->
                            PlanterCard(
                                planter = planter, gardenViewModel = gardenViewModel, selectedPots = selectedPots.keys,
                                langCode = langCode,
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

                if (showHarvestAnimation) {
                    HarvestAnimationOverlay()
                }
            }
        }

        if (showConfirmRemovalDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmRemovalDialog = false },
                icon = { Icon(imageVector = if(tipoAccionSeleccionada == "Recolectar") Icons.Default.ShoppingBasket else Icons.Default.Warning, contentDescription = null, tint = if(tipoAccionSeleccionada == "Recolectar") Color(0xFF4CAF50) else Color.Red) },
                title = { Text(text = if(tipoAccionSeleccionada == "Recolectar") strings.gardenActionHarvest else strings.gardenActionPullOut, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = if(tipoAccionSeleccionada == "Recolectar") strings.gardenConfirmRemovalHarvest else strings.gardenConfirmRemovalPullOut, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { showConfirmRemovalDialog = false }) { Text(strings.gardenNo) }
                            Spacer(Modifier.width(16.dp))
                            Button(onClick = {
                                val planterId = selectedPots.keys.first().first
                                val positions = selectedPots.keys.map { it.second to it.third }
                                gardenViewModel.manageFlowerpots(planterId, positions, null, tipoAccionSeleccionada)
                                vibrationHandler(200L)
                                if (tipoAccionSeleccionada == "Recolectar") showHarvestAnimation = true
                                showConfirmRemovalDialog = false
                                showPlantDialog = false
                                selectedPots = emptyMap()
                            }) { Text(strings.gardenYes) }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showConflictDialog) {
            AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800)) },
                title = { Text(text = strings.gardenConflictTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = conflictMessage, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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
                },
                confirmButton = {}
            )
        }

        if (showMultiplePlantersError) {
            AlertDialog(
                onDismissRequest = { showMultiplePlantersError = false; selectedPots = emptyMap() },
                icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF9800)) },
                title = { Text(strings.gardenMultiplePlantersErrorTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.gardenMultiplePlantersErrorText, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { showMultiplePlantersError = false; selectedPots = emptyMap() }) { Text(strings.gardenOk) }
                    }
                },
                confirmButton = {}
            )
        }

        if (showSelectionError) {
            AlertDialog(
                onDismissRequest = { showSelectionError = false; selectedPots = emptyMap() },
                icon = { Icon(Icons.Default.Info, null) },
                title = { Text(strings.gardenSelectionErrorTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.gardenSelectionErrorText, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { showSelectionError = false; selectedPots = emptyMap() }) { Text(strings.gardenOk) }
                    }
                },
                confirmButton = {}
            )
        }

        if (showPlantDialog) {
            val planterId = selectedPots.keys.first().first
            val currentOccupiedPots by gardenViewModel.getFlowerpots(planterId).collectAsState(initial = emptyList())
            AlertDialog(
                onDismissRequest = { showPlantDialog = false },
                title = { Text(text = strings.gardenManagePots, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.gardenManagePotsQuestion.replace("{0}", selectedPots.size.toString()), textAlign = TextAlign.Center)
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                            if (isHarvestMode) {
                                FilterChip(selected = tipoAccionSeleccionada == "Recolectar", onClick = { tipoAccionSeleccionada = "Recolectar" }, label = { Text(strings.gardenActionHarvest) }, leadingIcon = { Icon(Icons.Default.ShoppingBasket, null, tint = if(tipoAccionSeleccionada=="Recolectar") Color(0xFF4CAF50) else Color.Gray) })
                                FilterChip(selected = tipoAccionSeleccionada == "Arrancar", onClick = { tipoAccionSeleccionada = "Arrancar" }, label = { Text(strings.gardenActionPullOut) }, leadingIcon = { Icon(Icons.Default.Warning, null, tint = if(tipoAccionSeleccionada=="Arrancar") Color.Red else Color.Gray) })
                            } else {
                                listOf("Plantar", "Sembrar").forEach { action ->
                                    FilterChip(selected = tipoAccionSeleccionada == action, onClick = { tipoAccionSeleccionada = action }, label = { Text(if(action=="Plantar") strings.gardenActionPlant else strings.gardenActionSow) })
                                }
                            }
                        }
                        if (!isHarvestMode) {
                            Box {
                                OutlinedTextField(value = selectedPlantForPot?.nombreComun?.get(langCode) ?: strings.gardenSelectPlant, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { expandedPlantMenu = true }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline))
                                Box(Modifier.matchParentSize().clickable { expandedPlantMenu = true })
                                DropdownMenu(expanded = expandedPlantMenu, onDismissRequest = { expandedPlantMenu = false }) {
                                    availablePlants.forEach { plant -> DropdownMenuItem(text = { Text(plant.nombreComun.get(langCode)) }, onClick = { selectedPlantForPot = plant; expandedPlantMenu = false }) }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                        TextButton(onClick = { showPlantDialog = false }) { Text(strings.gardenCancel) }
                        Spacer(Modifier.width(16.dp))
                        Button(enabled = isHarvestMode || selectedPlantForPot != null, onClick = {
                            val positions = selectedPots.keys.map { it.second to it.third }
                            if (isHarvestMode) { showConfirmRemovalDialog = true; return@Button }
                            if (selectedPlantForPot != null) {
                                val conflict = checkConflicts(selectedPlantForPot!!, positions, currentOccupiedPots)
                                if (conflict != null) { conflictMessage = conflict; showConflictDialog = true; return@Button }
                            }
                            gardenViewModel.manageFlowerpots(planterId, positions, selectedPlantForPot, tipoAccionSeleccionada)
                            vibrationHandler(50L)
                            showPlantDialog = false
                            selectedPots = emptyMap()
                        }) { Text(strings.gardenConfirm) }
                    }
                }
            )
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text(strings.gardenNewPlanter) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(value = nombrePlanter, onValueChange = { nombrePlanter = it }, label = { Text(strings.gardenPlanterNamePlaceholder) }, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NumberSelector(strings.gardenRows, numFilas, { numFilas = it }, 1..2)
                            NumberSelector(strings.gardenCols, numColumnas, { numColumnas = it }, 1..8)
                        }
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

/**
 * Muestra una tarjeta con la información de una jardinera, incluyendo sus macetas y acciones.
 *
 * @param planter El objeto [Planter] a mostrar.
 * @param gardenViewModel El [GardenViewModel] para obtener los datos.
 * @param selectedPots El conjunto de macetas seleccionadas actualmente.
 * @param langCode El código de idioma para la localización.
 * @param onDelete Llama a esta función cuando se pulsa el botón de eliminar.
 * @param onEditName Llama a esta función cuando se pulsa el botón de editar nombre.
 * @param onPotClick Llama a esta función cuando se hace clic en una maceta.
 * @param onLogClick Llama a esta función para ver el registro de la jardinera.
 */
@Composable
fun PlanterCard(planter: Planter, gardenViewModel: GardenViewModel, selectedPots: Set<Triple<String, Int, Int>>, langCode: String, onDelete: () -> Unit, onEditName: () -> Unit, onPotClick: (Int, Int, Boolean) -> Unit, onLogClick: () -> Unit) {
    val strings = LocalStrings.current
    val shareHandler = rememberShareHandler()
    val occupiedPots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(planter.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onEditName, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp), MaterialTheme.colorScheme.outline) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val deepLinkUrl = "https://huertopedia.app/jardinera/${planter.id}"
                        // CORRECCIÓN: Usamos la etiqueta de traducción dinámica
                        val text = strings.sharePlanterMessage
                            .replace("{0}", planter.nombre)
                            .replace("{1}", occupiedPots.size.toString())
                            .replace("{2}", deepLinkUrl)
                        shareHandler(text)
                    }) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            Text(strings.gardenPlanterSize.replace("{0}", planter.filas.toString()).replace("{1}", planter.columnas.toString()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (f in 0 until planter.filas) {
                        Row(modifier = if (planter.columnas > 2) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                            for (c in 0 until planter.columnas) {
                                val pot = occupiedPots.find { it.fila == f && it.columna == c }
                                val itemModifier = if (planter.columnas > 2) Modifier.weight(1f) else Modifier.size(80.dp)
                                FlowerpotView(pot, selectedPots.contains(Triple(planter.id, f, c)), langCode, itemModifier) { onPotClick(f, c, pot != null) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onLogClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, null); Spacer(Modifier.width(8.dp)); Text(strings.gardenCropLog)
            }
        }
    }
}

/**
 * Muestra una única maceta en la jardinera, con su estado y contenido.
 *
 * @param pot El [GardenFlowerpot] a mostrar, o null si está vacía.
 * @param isSelected Indica si la maceta está seleccionada.
 * @param langCode El código de idioma para la localización.
 * @param modifier El modificador a aplicar.
 * @param onClick La acción a realizar al hacer clic en la maceta.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FlowerpotView(pot: GardenFlowerpot?, isSelected: Boolean, langCode: String, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = when {
        isSelected -> Color(0xFFFFF176)
        pot == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        pot.tipoAccion?.es == "Sembrar" -> Color(0xFFE1BEE7)
        else -> Color(0xFFC8E6C9)
    }
    Box(modifier = modifier.aspectRatio(1f).background(bgColor, RoundedCornerShape(8.dp)).border(1.dp, if(isSelected) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).clickable { onClick() }, Alignment.Center) {
        AnimatedContent(
            targetState = pot,
            transitionSpec = {
                if (targetState == null) {
                    fadeIn(tween(500)) togetherWith (scaleOut(tween(500), targetScale = 2.5f) + fadeOut(tween(500)) + slideOutVertically { -it * 2 })
                } else {
                    (scaleIn(spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.1f) + fadeIn()) togetherWith fadeOut(tween(200))
                }
            }
        ) { targetPot ->
            if (targetPot == null) {
                Icon(if(isSelected) Icons.Default.Check else Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                if (!targetPot.imagenUrl.isNullOrBlank()) KamelImage(asyncPainterResource(targetPot.imagenUrl!!), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(4.dp)))
                else Text(targetPot.nombrePlanta?.get(langCode)?.take(8) ?: "?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

/**
 * Un selector numérico que permite al usuario incrementar o decrementar un valor dentro de un rango.
 *
 * @param label La etiqueta a mostrar sobre el selector.
 * @param value El valor actual.
 * @param onValueChange Llama a esta función cuando el valor cambia.
 * @param range El rango de valores permitidos.
 */
@Composable
fun NumberSelector(label: String, value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Default.Remove, null) }
            Text(value.toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Default.Add, null) }
        }
    }
}

/**
 * Muestra una superposición con una animación de recolección de hojas.
 */
@Composable
fun HarvestAnimationOverlay() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerHeight = maxHeight
        val containerWidth = maxWidth
        val particles = remember { List(15) { LeafParticleData(startXRatio = Random.nextFloat(), delay = Random.nextLong(0, 500), duration = Random.nextInt(1000, 2000), icon = if (Random.nextBoolean()) Icons.Default.Eco else Icons.Default.Spa) } }
        particles.forEach { particle -> LeafParticle(particle, containerWidth, containerHeight) }
    }
}

/**
 * Contiene los datos para una partícula de hoja en la animación.
 *
 * @param startXRatio La posición X inicial relativa al ancho del contenedor.
 * @param delay El retardo antes de que comience la animación.
 * @param duration La duración de la animación.
 * @param icon El icono a mostrar para la partícula.
 */
data class LeafParticleData(val startXRatio: Float, val delay: Long, val duration: Int, val icon: ImageVector)

/**
 * Muestra una única partícula de hoja animada.
 *
 * @param data Los datos de la partícula [LeafParticleData].
 * @param containerWidth El ancho del contenedor padre.
 * @param containerHeight La altura del contenedor padre.
 */
@Composable
fun BoxScope.LeafParticle(data: LeafParticleData, containerWidth: androidx.compose.ui.unit.Dp, containerHeight: androidx.compose.ui.unit.Dp) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(data.delay)
        launch { offsetY.animateTo(targetValue = -containerHeight.value * 0.6f, animationSpec = tween(durationMillis = data.duration, easing = LinearOutSlowInEasing)) }
        launch { delay(data.duration / 2L); alpha.animateTo(0f, animationSpec = tween(durationMillis = data.duration / 2)) }
    }
    Icon(imageVector = data.icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.offset(x = (containerWidth * data.startXRatio) - 24.dp, y = offsetY.value.dp).align(Alignment.BottomStart).alpha(alpha.value).size(32.dp))
}
