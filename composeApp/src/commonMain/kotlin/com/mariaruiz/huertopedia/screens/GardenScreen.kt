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
    val myPlanters by gardenViewModel.planters.collectAsState(initial = emptyList())
    val availablePlants by gardenViewModel.availablePlants.collectAsState()

    // Estados
    var showCreateDialog by remember { mutableStateOf(false) }
    var nombrePlanter by remember { mutableStateOf("") }
    var numFilas by remember { mutableIntStateOf(1) }
    var numColumnas by remember { mutableIntStateOf(1) }

    // Key: Triple(PlanterId, Fila, Columna) -> Value: Boolean (¿Tiene planta?)
    var selectedPots by remember { mutableStateOf(emptyMap<Triple<String, Int, Int>, Boolean>()) }

    // Nuevo estado para el error de selección
    var showSelectionError by remember { mutableStateOf(false) }

    var showPlantDialog by remember { mutableStateOf(false) }
    var tipoAccionSeleccionada by remember { mutableStateOf("Plantar") }
    var selectedPlantForPot by remember { mutableStateOf<Plant?>(null) }
    var expandedPlantMenu by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var planterToDelete by remember { mutableStateOf<Planter?>(null) }

    // Lógica: Si alguna de las seleccionadas está ocupada, entramos en modo Recolectar
    val isHarvestMode = selectedPots.values.any { it }

    // Efecto: Cambia la acción por defecto automáticamente según la selección
    LaunchedEffect(isHarvestMode) {
        tipoAccionSeleccionada = if (isHarvestMode) "Recolectar" else "Plantar"
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Jardineras") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (selectedPots.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { showPlantDialog = true },
                        icon = { Icon(Icons.Default.Agriculture, null) },
                        text = { Text("Actividad en ${selectedPots.size}") },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Jardinera")
                }
            }
        }
    ) { padding ->

        // --- DIÁLOGO: ERROR DE SELECCIÓN ---
        if (showSelectionError) {
            AlertDialog(
                onDismissRequest = { showSelectionError = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text("Selección inválida") },
                text = {
                    Text(
                        text = "No puedes seleccionar macetas ocupadas y vacías al mismo tiempo.\n",
                        fontSize = 20.sp, // <--- AQUI CAMBIAS EL TAMAÑO
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showSelectionError = false }) {
                        Text("Entendido", fontSize = 16.sp) // También puedes aumentar el botón si quieres
                    }
                }
            )
        }

        // --- DIÁLOGO: CREAR JARDINERA ---
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nueva Jardinera") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = nombrePlanter,
                            onValueChange = { nombrePlanter = it },
                            label = { Text("Nombre (ej: Terraza)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        NumberSelector("Filas", numFilas, { numFilas = it }, GardenConfig.MIN_ROWS..GardenConfig.MAX_ROWS)
                        NumberSelector("Columnas", numColumnas, { numColumnas = it }, GardenConfig.MIN_COLS..GardenConfig.MAX_COLS)
                    }
                },
                confirmButton = {
                    Button(enabled = nombrePlanter.isNotBlank(), onClick = {
                        gardenViewModel.createPlanter(nombrePlanter, numFilas, numColumnas)
                        showCreateDialog = false
                        nombrePlanter = ""
                    }) { Text("Crear") }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") } }
            )
        }

        // --- DIÁLOGO: ACTIVIDADES ---
        if (showPlantDialog) {
            AlertDialog(
                onDismissRequest = { showPlantDialog = false },
                title = { Text(if (isHarvestMode) "Cosecha" else "Nueva plantación") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("¿Qué quieres hacer en las ${selectedPots.size} macetas?", style = MaterialTheme.typography.bodyMedium)

                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isHarvestMode) {
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Recolectar",
                                    onClick = { tipoAccionSeleccionada = "Recolectar" },
                                    label = { Text("Recolectar") },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Recolectar") Icon(Icons.Default.Check, null) }
                                )
                            } else {
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Plantar",
                                    onClick = { tipoAccionSeleccionada = "Plantar" },
                                    label = { Text("Plantar") },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Plantar") Icon(Icons.Default.Check, null) }
                                )
                                FilterChip(
                                    selected = tipoAccionSeleccionada == "Sembrar",
                                    onClick = { tipoAccionSeleccionada = "Sembrar" },
                                    label = { Text("Sembrar") },
                                    leadingIcon = { if(tipoAccionSeleccionada=="Sembrar") Icon(Icons.Default.Check, null) }
                                )
                            }
                        }

                        if (!isHarvestMode) {
                            Box(Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedPlantForPot?.nombreComun ?: "Selecciona planta",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { expandedPlantMenu = true },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline)
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
                    ) { Text("Confirmar") }
                },
                dismissButton = { TextButton(onClick = { showPlantDialog = false }) { Text("Cancelar") } }
            )
        }

        // --- DIÁLOGO: CONFIRMAR BORRADO ---
        if (showDeleteConfirm && planterToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar") },
                text = { Text("¿Borrar '${planterToDelete?.nombre}'?") },
                confirmButton = {
                    Button(onClick = {
                        planterToDelete?.id?.let { gardenViewModel.deletePlanter(it) }
                        showDeleteConfirm = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
            )
        }

        if (myPlanters.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes jardineras aún.")
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
                        onPotClick = { f, c, isOccupied ->
                            val triple = Triple(planter.id, f, c)

                            // LÓGICA DE VALIDACIÓN MEJORADA
                            if (selectedPots.containsKey(triple)) {
                                // Si ya existe, deseleccionamos sin problema
                                selectedPots = selectedPots - triple
                            } else {
                                // Si vamos a añadir, verificamos coherencia
                                if (selectedPots.isNotEmpty()) {
                                    val currentModeIsOccupied = selectedPots.values.first()
                                    if (currentModeIsOccupied != isOccupied) {
                                        // ERROR: Intenta mezclar estados -> Mostramos alerta
                                        showSelectionError = true
                                    } else {
                                        // OK: Coincide con el modo actual
                                        selectedPots = selectedPots + (triple to isOccupied)
                                    }
                                } else {
                                    // OK: Primera selección
                                    selectedPots = selectedPots + (triple to isOccupied)
                                }
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
    onPotClick: (Int, Int, Boolean) -> Unit,
    onLogClick: () -> Unit
) {
    val occupiedPots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(planter.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${planter.filas} x ${planter.columnas}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }

            Spacer(Modifier.height(12.dp))

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
                                onClick = {
                                    onPotClick(f, c, pot != null)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Diario de Cultivo")
            }
        }
    }
}

@Composable
fun FlowerpotView(pot: GardenFlowerpot?, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = when {
        isSelected -> Color(0xFFFFF176) // Amarillo selección
        pot == null -> Color.LightGray.copy(alpha = 0.3f)
        pot.tipoAccion == "Sembrar" -> Color(0xFFE1BEE7) // Violeta para Sembrar
        else -> Color(0xFFE8F5E9) // Verde para Plantar
    }

    val borderColor = when {
        isSelected -> Color(0xFFFBC02D)
        pot == null -> Color.Gray
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
            Icon(if (isSelected) Icons.Default.Check else Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        } else {
            if (!pot.imagenUrl.isNullOrBlank()) {
                KamelImage(
                    resource = asyncPainterResource(data = pot.imagenUrl!!),
                    contentDescription = pot.nombrePlanta,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(4.dp))
                )
            } else {
                Text(pot.nombrePlanta?.take(8) ?: "?", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
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