package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.utils.toHumanDateString
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLogScreen(
    planter: Planter,
    onBack: () -> Unit,
    gardenViewModel: GardenViewModel
) {
    val flowerpots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

    // Estados para el Diálogo
    var showAddDialog by remember { mutableStateOf(false) }

    // Lista de tipos de evento
    val eventTypes = listOf(
        "Notas",
        "Riego",
        "Germinación",
        "Enfermedad",
        "Fertilización",
        "Entutorado",
        "Eliminación adventicias"
    )

    // Variables del formulario
    var selectedEventType by remember { mutableStateOf(eventTypes[0]) }
    var expandedEventType by remember { mutableStateOf(false) }
    var notesContent by remember { mutableStateOf("") }

    // Variables específicas para Riego
    var irrigationType by remember { mutableStateOf("Manual") }
    var irrigationMinutes by remember { mutableFloatStateOf(10f) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diario: ${planter.nombre}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Resetear formulario
                    selectedEventType = "Notas"
                    notesContent = ""
                    irrigationMinutes = 10f
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir entrada")
            }
        }
    ) { padding ->

        // --- DIÁLOGO DE NUEVA ENTRADA ---
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nueva Entrada") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. SELECTOR DE TIPO
                        ExposedDropdownMenuBox(
                            expanded = expandedEventType,
                            onExpandedChange = { expandedEventType = !expandedEventType }
                        ) {
                            OutlinedTextField(
                                value = selectedEventType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Evento") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventType) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedEventType,
                                onDismissRequest = { expandedEventType = false }
                            ) {
                                eventTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedEventType = type
                                            expandedEventType = false
                                        }
                                    )
                                }
                            }
                        }

                        Divider()

                        // 2. CONTROLES DINÁMICOS
                        when (selectedEventType) {
                            "Riego" -> {
                                Text("Método de Riego", style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Manual", "Goteo", "Lluvia").forEach { type ->
                                        FilterChip(
                                            selected = irrigationType == type,
                                            onClick = { irrigationType = type },
                                            label = { Text(type) },
                                            leadingIcon = {
                                                if (irrigationType == type) Icon(Icons.Default.WaterDrop, null)
                                            }
                                        )
                                    }
                                }

                                Text("Duración: ${irrigationMinutes.roundToInt()} min", style = MaterialTheme.typography.bodyMedium)
                                Slider(
                                    value = irrigationMinutes,
                                    onValueChange = { irrigationMinutes = it },
                                    valueRange = 0f..60f,
                                    steps = 11
                                )
                            }

                            "Notas", "Enfermedad" -> {
                                OutlinedTextField(
                                    value = notesContent,
                                    onValueChange = { notesContent = it },
                                    label = { Text(if(selectedEventType == "Enfermedad") "Síntomas..." else "Escribe tus notas...") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    maxLines = 5
                                )

                                // --- BOTÓN DE CÁMARA (VISUAL SOLAMENTE) ---
                                OutlinedButton(
                                    onClick = { /* TODO añadir acción abrir cámara de fotos */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Añadir foto")
                                }
                            }

                            else -> {
                                Text("Evento: '${selectedEventType}' con fecha de hoy.", style = MaterialTheme.typography.bodySmall)
                                OutlinedTextField(
                                    value = notesContent,
                                    onValueChange = { notesContent = it },
                                    label = { Text("Observaciones (Opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // AQUÍ GUARDARÍAS
                        showAddDialog = false
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // Lista de entradas existentes
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(flowerpots) { pot ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Text("Maceta ${pot.fila + 1},${pot.columna + 1}: ${pot.nombrePlanta}")
                        },
                        supportingContent = {
                            Text("${pot.tipoAccion} el ${pot.fechaSiembra?.toHumanDateString()}")
                        }
                    )
                }
            }
            if (flowerpots.isEmpty()) {
                item {
                    Text("No hay actividades registradas.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}