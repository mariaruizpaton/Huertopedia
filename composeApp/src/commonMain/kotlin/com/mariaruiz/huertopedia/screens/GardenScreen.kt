package com.mariaruiz.huertopedia.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.model.GardenConfig
import com.mariaruiz.huertopedia.model.GardenFlowerpot
import com.mariaruiz.huertopedia.model.Plant
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import com.mariaruiz.huertopedia.i18n.LocalStrings
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

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

    var showPlantDialog by remember { mutableStateOf(false) }
    var tipoAccionSeleccionada by remember { mutableStateOf("Plantar") }
    var selectedPlantForPot by remember { mutableStateOf<Plant?>(null) }
    var expandedPlantMenu by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var planterToDelete by remember { mutableStateOf<Planter?>(null) }

    val isHarvestMode = selectedPots.values.any { it }

    LaunchedEffect(isHarvestMode) {
        tipoAccionSeleccionada = if (isHarvestMode) "Recolectar" else "Plantar"
    }

    BackHandler { onBack() }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedPots.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showPlantDialog = true },
                        icon = { Icon(Icons.Default.Agriculture, null) },
                        text = { Text(strings.gardenActivityButton.replace("{0}", selectedPots.size.toString())) },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.gardenAddPlanter)
                }
            }
        }
    ) { padding ->

        // --- DIÁLOGO: DIFERENTES JARDINERAS (PRIORITARIO) ---
        if (showMultiplePlantersError) {
            AlertDialog(
                onDismissRequest = { 
                    showMultiplePlantersError = false 
                    selectedPots = emptyMap()
                },
                icon = { Icon(Icons.Default.Info, null, tint = Color(0xFFFF9800)) },
                title = { Text(strings.gardenMultiplePlantersErrorTitle) },
                text = { Text(text = strings.gardenMultiplePlantersErrorText, fontSize = 18.sp) },
                confirmButton = {
                    TextButton(onClick = { 
                        showMultiplePlantersError = false 
                        selectedPots = emptyMap()
                    }) { Text(strings.gardenOk) }
                }
            )
        }

        // --- DIÁLOGO: ERROR DE SELECCIÓN (OCUPADA/VACÍA) ---
        if (showSelectionError) {
            AlertDialog(
                onDismissRequest = {
                    showSelectionError = false
                    selectedPots = emptyMap()
                },
                icon = { Icon(Icons.Default.Info, null) },
                title = { Text(strings.gardenSelectionErrorTitle) },
                text = { Text(text = strings.gardenSelectionErrorText, fontSize = 18.sp) },
                confirmButton = {
                    TextButton(onClick = {
                        showSelectionError = false
                        selectedPots = emptyMap()
                    }) { Text(strings.gardenOk) }
                }
            )
        }

        if (showEditDialog && planterToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text(strings.gardenEditNameTitle) },
                text = {
                    OutlinedTextField(
                        value = nuevoNombrePlanter,
                        onValueChange = { nuevoNombrePlanter = it },
                        label = { Text(strings.gardenNewNameLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        enabled = nuevoNombrePlanter.isNotBlank(),
                        onClick = {
                            planterToEdit?.id?.let { gardenViewModel.updatePlanterName(it, nuevoNombrePlanter) }
                            showEditDialog = false
                        }
                    ) { Text(strings.gardenSave) }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text(strings.gardenCancel) }
                }
            )
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text(strings.gardenNewPlanter) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = nombrePlanter,
                            onValueChange = { nombrePlanter = it },
                            label = { Text(strings.gardenPlanterNamePlaceholder) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        NumberSelector(strings.gardenRows, numFilas, { numFilas = it }, GardenConfig.MIN_ROWS..GardenConfig.MAX_ROWS)
                        NumberSelector(strings.gardenCols, numColumnas, { numColumnas = it }, GardenConfig.MIN_COLS..GardenConfig.MAX_COLS)
                    }
                },
                confirmButton = {
                    Button(enabled = nombrePlanter.isNotBlank(), onClick = {
                        gardenViewModel.createPlanter(nombrePlanter, numFilas, numColumnas)
                        showCreateDialog = false
                        nombrePlanter = ""
                    }) { Text(strings.gardenCreate) }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text(strings.gardenCancel) } }
            )
        }

        if (showPlantDialog) {
            AlertDialog(
                onDismissRequest = { showPlantDialog = false },
                title = { Text(strings.gardenManagePots) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(strings.gardenManagePotsQuestion.replace("{0}", selectedPots.size.toString()), style = MaterialTheme.typography.bodyMedium)
                        
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isHarvestMode) {
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Recolectar",
                                    onClick = { tipoAccionSeleccionada = "Recolectar" },
                                    label = { Text(strings.gardenActionHarvest) },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Recolectar") Icon(Icons.Default.Check, null) }
                                )
                            } else {
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Plantar",
                                    onClick = { tipoAccionSeleccionada = "Plantar" },
                                    label = { Text(strings.gardenActionPlant) },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Plantar") Icon(Icons.Default.Check, null) }
                                )
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Sembrar",
                                    onClick = { tipoAccionSeleccionada = "Sembrar" },
                                    label = { Text(strings.gardenActionSow) },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Sembrar") Icon(Icons.Default.Check, null) }
                                )
                            }
                        }

                        if (!isHarvestMode) {
                            Box(Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedPlantForPot?.nombreComun ?: strings.gardenSelectPlant,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { expandedPlantMenu = true },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface, 
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Box(Modifier.matchParentSize().clickable { expandedPlantMenu = true })
                                DropdownMenu(expanded = expandedPlantMenu, onDismissRequest = { expandedPlantMenu = false }) {
                                    availablePlants.forEach { plant ->
                                        DropdownMenuItem(text = { Text(plant.nombreComun) }, onClick = {
                                            selectedPlantForPot = plant
                                            expandedPlantMenu = false
                                        })
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = isHarvestMode || selectedPlantForPot != null,
                        onClick = {
                            val planterId = selectedPots.keys.first().first
                            val positions = selectedPots.keys.map { it.second to it.third }
                            gardenViewModel.manageFlowerpots(planterId, positions, selectedPlantForPot, tipoAccionSeleccionada)
                            showPlantDialog = false
                            selectedPots = emptyMap()
                            selectedPlantForPot = null
                        }
                    ) { Text(strings.gardenConfirm) }
                },
                dismissButton = { TextButton(onClick = { showPlantDialog = false }) { Text(strings.gardenCancel) } }
            )
        }

        if (showDeleteConfirm && planterToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(strings.gardenDelete) },
                text = { Text(strings.gardenDeleteConfirmQuestion.replace("{0}", planterToDelete?.nombre ?: "")) },
                confirmButton = {
                    Button(onClick = {
                        planterToDelete?.id?.let { gardenViewModel.deletePlanter(it) }
                        showDeleteConfirm = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(strings.gardenDelete) }
                },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.gardenCancel) } }
            )
        }

        // --- LISTA DE JARDINERAS ---
        if (myPlanters.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.gardenNoPlanters, color = MaterialTheme.colorScheme.onBackground)
            }
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
                        onEditName = { 
                            planterToEdit = planter
                            nuevoNombrePlanter = planter.nombre
                            showEditDialog = true
                        },
                        onPotClick = { f, c, isOccupied ->
                            val triple = Triple(planter.id, f, c)
                            if (selectedPots.containsKey(triple)) {
                                selectedPots = selectedPots - triple
                            } else {
                                if (selectedPots.isNotEmpty()) {
                                    val firstSelectedId = selectedPots.keys.first().first
                                    if (firstSelectedId != planter.id) {
                                        showMultiplePlantersError = true
                                        return@PlanterCard
                                    }
                                    val currentModeIsOccupied = selectedPots.values.first()
                                    if (currentModeIsOccupied != isOccupied) {
                                        showSelectionError = true
                                        return@PlanterCard
                                    }
                                }
                                selectedPots = selectedPots + (triple to isOccupied)
                            }
                        },
                        onLogClick = { onNavigateToLog(planter) }
                    )
                }
            }
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
    val occupiedPots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(planter.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onEditName, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = strings.gardenEditNameTitle, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                    }
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
            Text(
                text = strings.gardenPlanterSize
                    .replace("{0}", planter.filas.toString())
                    .replace("{1}", planter.columnas.toString()), 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (f in 0 until planter.filas) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until planter.columnas) {
                            val pot = occupiedPots.find { it.fila == f && it.columna == c }
                            val isSelected = selectedPots.contains(Triple(planter.id, f, c))
                            FlowerpotView(
                                pot = pot,
                                isSelected = isSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { onPotClick(f, c, pot != null) }
                            )
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
        else -> Color(0xFFE8F5E9)
    }
    
    val borderColor = when {
        isSelected -> Color(0xFFFBC02D)
        pot == null -> MaterialTheme.colorScheme.outline
        pot.tipoAccion == "Sembrar" -> Color(0xFF9C27B0)
        else -> Color(0xFF4CAF50)
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (pot == null) {
            Icon(if (isSelected) Icons.Default.Check else Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        } else {
            if (!pot.imagenUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(data = pot.imagenUrl!!),
                    contentDescription = pot.nombrePlanta,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(4.dp))
                )
            } else {
                Text(
                    text = pot.nombrePlanta?.take(8) ?: "?", 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun NumberSelector(label: String, value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Default.Remove, null) }
            Text(value.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Default.Add, null) }
        }
    }
}
