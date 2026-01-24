package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.i18n.AppStrings
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.model.CropLog
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.utils.getCurrentTimeMillis
import com.mariaruiz.huertopedia.utils.toHumanDateString
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.IntrinsicSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLogScreen(
    planter: Planter,
    onBack: () -> Unit,
    gardenViewModel: GardenViewModel
) {
    val strings = LocalStrings.current
    val cropLogs by gardenViewModel.observeCropLogs(planter.id).collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<CropLog?>(null) }

    val eventTypes = listOf(
        strings.eventNotes,
        strings.eventIrrigation,
        strings.eventGermination,
        strings.eventDisease,
        strings.eventFertilization,
        strings.eventTrellising,
        strings.eventWeeding
    )

    var selectedEventType by remember { mutableStateOf(eventTypes[0]) }
    var expandedEventType by remember { mutableStateOf(false) }
    var notesContent by remember { mutableStateOf("") }
    var irrigationType by remember { mutableStateOf(strings.irrigationManual) }
    var irrigationMinutes by remember { mutableFloatStateOf(10f) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(strings.cropLogTitle.replace("{0}", ""), style = MaterialTheme.typography.headlineMedium)
                        Text(planter.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.cropLogNewEntry)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                title = { Text(strings.cropLogNewEntry, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- Selector de Tipo de Evento ---
                        ExposedDropdownMenuBox(expanded = expandedEventType, onExpandedChange = { expandedEventType = !expandedEventType }) {
                            OutlinedTextField(
                                value = selectedEventType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.cropLogEventType) },
                                leadingIcon = { Icon(getEventIcon(selectedEventType, strings), contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventType) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = expandedEventType, onDismissRequest = { expandedEventType = false }) {
                                eventTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        leadingIcon = { Icon(getEventIcon(type, strings), null, tint = getEventColor(type, strings)) },
                                        onClick = {
                                            selectedEventType = type
                                            expandedEventType = false
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- Formulario Cambiante ---
                        when (selectedEventType) {
                            // Caso Riego (Se mantiene igual)
                            strings.eventIrrigation -> {
                                Text(strings.cropLogIrrigationMethod, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    listOf(strings.irrigationManual, strings.irrigationDrip, strings.irrigationRain).forEach { type ->
                                        FilterChip(
                                            selected = irrigationType == type,
                                            onClick = { irrigationType = type },
                                            label = { Text(type) },
                                            leadingIcon = { if (irrigationType == type) Icon(Icons.Default.Check, null) }
                                        )
                                    }
                                }
                                Text("${irrigationMinutes.roundToInt()} min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Slider(value = irrigationMinutes, onValueChange = { irrigationMinutes = it }, valueRange = 0f..60f, steps = 11)
                            }
                            // Caso Resto (Notas, Plagas, etc.) - AQUÍ ESTÁ EL CAMBIO
                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = notesContent,
                                        onValueChange = { notesContent = it },
                                        label = { Text(strings.cropLogWriteNotes) },
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        maxLines = 5,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // --- NUEVO: Botón de Cámara solo para Notas y Plagas ---
                                    if (selectedEventType == strings.eventNotes || selectedEventType == strings.eventDisease) {
                                        OutlinedButton(
                                            onClick = {
                                                // TODO: Implementar lógica de cámara en el futuro
                                                println("Click en cámara para: $selectedEventType")
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Añadir foto") // Texto hardcoded por ahora
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val noteForLog = if (selectedEventType == strings.eventIrrigation) {
                            "$irrigationType, ${irrigationMinutes.roundToInt()} min"
                        } else {
                            notesContent.takeIf { it.isNotBlank() }
                        }
                        val newLog = CropLog(
                            planterId = planter.id,
                            timestamp = getCurrentTimeMillis(),
                            eventType = selectedEventType,
                            notes = noteForLog,
                            irrigationType = irrigationType.takeIf { selectedEventType == strings.eventIrrigation },
                            irrigationMinutes = irrigationMinutes.roundToInt().takeIf { selectedEventType == strings.eventIrrigation }
                        )
                        gardenViewModel.addCropLogEntry(newLog)
                        showAddDialog = false
                        notesContent = ""
                        selectedEventType = eventTypes[0]
                        irrigationType = strings.irrigationManual
                        irrigationMinutes = 10f
                    }) { Text(strings.gardenSave) }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text(strings.gardenCancel) } }
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, null) },
                title = { Text(strings.cropLogDeleteEntryTitle) },
                text = { Text(strings.cropLogDeleteEntry) },
                confirmButton = {
                    Button(onClick = {
                        logToDelete?.let { gardenViewModel.deleteCropLogEntry(it.planterId, it.id) }
                        showDeleteDialog = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(strings.gardenDelete) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(strings.gardenCancel) } }
            )
        }
        if (cropLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(strings.cropLogNoEntries, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val sortedLogs = cropLogs.sortedByDescending { it.timestamp }
                items(sortedLogs) { log ->
                    TimelineItem(
                        log = log,
                        strings = strings,
                        onDeleteClick = {
                            logToDelete = log
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    log: CropLog,
    strings: AppStrings,
    onDeleteClick: () -> Unit
) {
    val eventColor = getEventColor(log.eventType, strings)
    val eventIcon = getEventIcon(log.eventType, strings)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(eventColor.copy(alpha = 0.2f)).border(2.dp, eventColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = eventIcon, contentDescription = null, tint = eventColor, modifier = Modifier.size(16.dp))
            }
            Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Card(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = log.eventType, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(text = log.timestamp.toHumanDateString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (!log.notes.isNullOrBlank()) {
                    Text(text = log.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// --- HELPERS ---

@Composable
fun getEventColor(eventType: String, strings: AppStrings): Color {
    return when (eventType) {
        strings.eventIrrigation -> Color(0xFF2196F3)
        strings.eventDisease -> Color(0xFFF44336)
        strings.eventFertilization -> Color(0xFF795548)
        strings.eventGermination -> Color(0xFF4CAF50)
        strings.eventWeeding -> Color(0xFF8BC34A)
        strings.eventTrellising -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun getEventIcon(eventType: String, strings: AppStrings): ImageVector {
    return when (eventType) {
        strings.eventIrrigation -> Icons.Default.WaterDrop
        strings.eventDisease -> Icons.Default.BugReport
        strings.eventFertilization -> Icons.Default.Science
        strings.eventGermination -> Icons.Default.Spa
        strings.eventWeeding -> Icons.Default.ContentCut
        strings.eventTrellising -> Icons.Default.Height
        else -> Icons.Default.EditNote
    }
}