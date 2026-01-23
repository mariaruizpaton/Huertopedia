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
import com.mariaruiz.huertopedia.i18n.LocalStrings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLogScreen(
    planter: Planter,
    onBack: () -> Unit,
    gardenViewModel: GardenViewModel
) {
    val strings = LocalStrings.current
    val flowerpots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

    // Estados para el Diálogo
    var showAddDialog by remember { mutableStateOf(false) }

    // Lista de tipos de evento traducida
    val eventTypes = listOf(
        strings.eventNotes,
        strings.eventIrrigation,
        strings.eventGermination,
        strings.eventDisease,
        strings.eventFertilization,
        strings.eventTrellising,
        strings.eventWeeding
    )

    // Variables del formulario
    var selectedEventType by remember { mutableStateOf(eventTypes[0]) }
    var expandedEventType by remember { mutableStateOf(false) }
    var notesContent by remember { mutableStateOf("") }

    // Variables específicas para Riego
    var irrigationType by remember { mutableStateOf(strings.irrigationManual) }
    var irrigationMinutes by remember { mutableFloatStateOf(10f) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.cropLogTitle.replace("{0}", planter.nombre)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedEventType = eventTypes[0]
                    notesContent = ""
                    irrigationMinutes = 10f
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.cropLogAddEntry)
            }
        }
    ) { padding ->

        // --- DIÁLOGO DE NUEVA ENTRADA ---
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(strings.cropLogNewEntry) },
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
                                label = { Text(strings.cropLogEventType) },
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
                            strings.eventIrrigation -> {
                                Text(strings.cropLogIrrigationMethod, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(strings.irrigationManual, strings.irrigationDrip, strings.irrigationRain).forEach { type ->
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

                                Text(strings.cropLogIrrigationDuration.replace("{0}", irrigationMinutes.roundToInt().toString()), style = MaterialTheme.typography.bodyMedium)
                                Slider(
                                    value = irrigationMinutes,
                                    onValueChange = { irrigationMinutes = it },
                                    valueRange = 0f..60f,
                                    steps = 11
                                )
                            }

                            strings.eventNotes, strings.eventDisease -> {
                                OutlinedTextField(
                                    value = notesContent,
                                    onValueChange = { notesContent = it },
                                    label = { Text(if(selectedEventType == strings.eventDisease) strings.cropLogSymptoms else strings.cropLogWriteNotes) },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    maxLines = 5
                                )

                                OutlinedButton(
                                    onClick = { /* Cámara */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.cropLogAddPhoto)
                                }
                            }

                            else -> {
                                Text(strings.cropLogEventToday.replace("{0}", selectedEventType), style = MaterialTheme.typography.bodySmall)
                                OutlinedTextField(
                                    value = notesContent,
                                    onValueChange = { notesContent = it },
                                    label = { Text(strings.cropLogObservations) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAddDialog = false }) { Text(strings.gardenSave) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text(strings.gardenCancel) }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(flowerpots) { pot ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Text(
                                strings.cropLogEntry
                                    .replace("{0}", (pot.fila + 1).toString())
                                    .replace("{1}", (pot.columna + 1).toString())
                                    .replace("{2}", pot.nombrePlanta ?: "?")
                            )
                        },
                        supportingContent = {
                            val action = when(pot.tipoAccion) {
                                "Plantar" -> strings.gardenActionPlant
                                "Sembrar" -> strings.gardenActionSow
                                else -> pot.tipoAccion ?: ""
                            }
                            val dateStr = pot.fechaSiembra?.toHumanDateString() ?: strings.cropLogRecently
                            Text(
                                strings.cropLogStatus
                                    .replace("{0}", action)
                                    .replace("{1}", dateStr)
                            )
                        }
                    )
                }
            }
            if (flowerpots.isEmpty()) {
                item {
                    Text(strings.cropLogNoEntries, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
