package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mariaruiz.huertopedia.i18n.AppStrings
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.model.CropLog
import com.mariaruiz.huertopedia.model.LocalizedText
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.utils.getCurrentTimeMillis
import com.mariaruiz.huertopedia.utils.toHumanDateString
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel
import kotlin.math.roundToInt
import com.mariaruiz.huertopedia.utils.rememberCameraLauncher
import com.mariaruiz.huertopedia.utils.rememberImagePicker
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch

/**
 * Muestra la pantalla del registro de cultivo para una jardinera específica.
 *
 * @param planter La jardinera para la que se muestra el registro.
 * @param onBack Llama a esta función para navegar hacia atrás.
 * @param gardenViewModel El [GardenViewModel] para gestionar los datos del registro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLogScreen(
    planter: Planter,
    onBack: () -> Unit,
    gardenViewModel: GardenViewModel
) {
    val strings = LocalStrings.current
    val langCode = strings.changeLanguage.takeLast(2).lowercase()
    val cropLogs by gardenViewModel.observeCropLogs(planter.id).collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<CropLog?>(null) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val eventTypesMap = remember {
        listOf(
            strings.eventNotes to LocalizedText("Notas", "Notes"),
            strings.eventIrrigation to LocalizedText("Riego", "Irrigation"),
            strings.eventGermination to LocalizedText("Germinación", "Germination"),
            strings.eventDisease to LocalizedText("Enfermedad", "Disease"),
            strings.eventFertilization to LocalizedText("Fertilización", "Fertilization"),
            strings.eventTrellising to LocalizedText("Entutorado", "Trellising"),
            strings.eventWeeding to LocalizedText("Eliminación adventicias", "Weeding")
        )
    }

    var selectedEventTypePair by remember { mutableStateOf(eventTypesMap[0]) }
    var expandedEventType by remember { mutableStateOf(false) }
    var notesContent by remember { mutableStateOf("") }
    
    val irrigationTypesMap = remember {
        listOf(
            strings.irrigationManual to LocalizedText("Manual", "Manual"),
            strings.irrigationDrip to LocalizedText("Goteo", "Drip"),
            strings.irrigationRain to LocalizedText("Lluvia", "Rain")
        )
    }
    var selectedIrrigationPair by remember { mutableStateOf(irrigationTypesMap[0]) }
    var irrigationMinutes by remember { mutableFloatStateOf(10f) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)
    val cameraLauncher = rememberCameraLauncher { bytes -> selectedImageBytes = bytes }

    BackHandler { onBack() }

    if (selectedImageUrl != null) {
        Dialog(onDismissRequest = { selectedImageUrl = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                KamelImage(resource = asyncPainterResource(selectedImageUrl!!), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().clickable { selectedImageUrl = null })
                IconButton(onClick = { selectedImageUrl = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(strings.cropLogTitle.replace("{0}", ""), style = MaterialTheme.typography.headlineMedium)
                        Text(planter.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack) } },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Default.Add, contentDescription = strings.cropLogAddEntry)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                title = { Text(strings.cropLogNewEntry, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ExposedDropdownMenuBox(expanded = expandedEventType, onExpandedChange = { expandedEventType = !expandedEventType }) {
                            OutlinedTextField(
                                value = selectedEventTypePair.first,
                                onValueChange = {}, readOnly = true,
                                label = { Text(strings.cropLogEventType) },
                                leadingIcon = { Icon(getEventIcon(selectedEventTypePair.second.es), contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventType) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = expandedEventType, onDismissRequest = { expandedEventType = false }) {
                                eventTypesMap.forEach { pair ->
                                    DropdownMenuItem(
                                        text = { Text(pair.first) },
                                        leadingIcon = { Icon(getEventIcon(pair.second.es), null, tint = getEventColor(pair.second.es)) },
                                        onClick = { selectedEventTypePair = pair; expandedEventType = false }
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        if (selectedEventTypePair.first == strings.eventIrrigation) {
                            Text(strings.cropLogIrrigationMethod, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                irrigationTypesMap.forEach { pair ->
                                    FilterChip(
                                        selected = selectedIrrigationPair == pair,
                                        onClick = { selectedIrrigationPair = pair },
                                        label = { Text(pair.first) },
                                        leadingIcon = { if (selectedIrrigationPair == pair) Icon(Icons.Default.Check, null) }
                                    )
                                }
                            }
                            Text("${irrigationMinutes.roundToInt()} min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Slider(value = irrigationMinutes, onValueChange = { irrigationMinutes = it }, valueRange = 0f..60f, steps = 11)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(value = notesContent, onValueChange = { notesContent = it }, label = { Text(strings.cropLogWriteNotes) }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5, shape = RoundedCornerShape(12.dp))
                                if (selectedEventTypePair.first == strings.eventNotes || selectedEventTypePair.first == strings.eventDisease) {
                                    OutlinedButton(onClick = { scope.launch { try { controller.providePermission(Permission.CAMERA); cameraLauncher.capture() } catch (e: Exception) { e.printStackTrace() } } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                        if (selectedImageBytes != null) {
                                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Foto adjuntada")
                                        } else {
                                            Icon(Icons.Default.PhotoCamera, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(strings.cropLogAddPhoto)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val isIrrigation = selectedEventTypePair.first == strings.eventIrrigation
                        val finalNotes = if (isIrrigation) {
                            val mins = irrigationMinutes.roundToInt()
                            LocalizedText(
                                es = "${selectedIrrigationPair.second.es}, $mins min",
                                en = "${selectedIrrigationPair.second.en}, $mins min"
                            )
                        } else {
                            LocalizedText(es = notesContent, en = notesContent)
                        }
                        
                        val newLog = CropLog(
                            planterId = planter.id,
                            timestamp = getCurrentTimeMillis(),
                            eventType = selectedEventTypePair.second,
                            notes = finalNotes,
                            irrigationType = if(isIrrigation) selectedIrrigationPair.second else null,
                            irrigationMinutes = if(isIrrigation) irrigationMinutes.roundToInt() else null,
                            photoPath = null
                        )
                        gardenViewModel.addCropLogEntry(newLog, selectedImageBytes)
                        showAddDialog = false
                        notesContent = ""
                        selectedImageBytes = null
                        selectedEventTypePair = eventTypesMap[0]
                        selectedIrrigationPair = irrigationTypesMap[0]
                        irrigationMinutes = 10f
                    }) { Text(strings.gardenSave) }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false; selectedImageBytes = null }) { Text(strings.gardenCancel) } }
            )
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, null) },
                title = { Text(strings.cropLogDeleteEntryTitle) },
                text = { Text(strings.cropLogDeleteEntry) },
                confirmButton = {
                    Button(onClick = { logToDelete?.let { gardenViewModel.deleteCropLogEntry(it.planterId, it.id, it.photoPath) }; showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(strings.gardenDelete) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(strings.gardenCancel) } }
            )
        }

        if (cropLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryEdu, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(strings.cropLogNoEntries, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(cropLogs.sortedByDescending { it.timestamp }) { log ->
                    TimelineItem(log = log, langCode = langCode, onDeleteClick = { logToDelete = log; showDeleteDialog = true }, onImageClick = { url -> selectedImageUrl = url })
                }
            }
        }
    }
}

/**
 * Muestra un elemento en la línea de tiempo del registro de cultivo.
 *
 * @param log El [CropLog] a mostrar.
 * @param langCode El código de idioma para la localización.
 * @param onDeleteClick Llama a esta función cuando se pulsa el botón de eliminar.
 * @param onImageClick Llama a esta función cuando se hace clic en una imagen.
 */
@Composable
fun TimelineItem(log: CropLog, langCode: String, onDeleteClick: () -> Unit, onImageClick: (String) -> Unit) {
    val eventName = log.eventType.get(langCode)
    val eventColor = getEventColor(log.eventType.es)
    val eventIcon = getEventIcon(log.eventType.es)
    
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(eventColor.copy(alpha = 0.2f)).border(2.dp, eventColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = eventIcon, contentDescription = null, tint = eventColor, modifier = Modifier.size(16.dp))
            }
            Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = eventName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(text = log.timestamp.toHumanDateString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (log.notes != null) {
                    val noteText = log.notes.get(langCode)
                    if (noteText.isNotBlank()) {
                        Text(text = noteText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (log.photoPath != null) {
                    KamelImage(resource = asyncPainterResource(log.photoPath!!), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).clickable { onImageClick(log.photoPath!!) })
                    Spacer(modifier = Modifier.height(8.dp))
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

/**
 * Devuelve un color para un tipo de evento específico.
 *
 * @param eventEs El nombre del evento en español.
 * @return El [Color] asociado al evento.
 */
fun getEventColor(eventEs: String): Color {
    return when (eventEs) {
        "Riego" -> Color(0xFF2196F3)
        "Enfermedad" -> Color(0xFFF44336)
        "Fertilización" -> Color(0xFF795548)
        "Germinación" -> Color(0xFF4CAF50)
        "Eliminación adventicias" -> Color(0xFF8BC34A)
        "Entutorado" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

/**
 * Devuelve un icono para un tipo de evento específico.
 *
 * @param eventEs El nombre del evento en español.
 * @return El [ImageVector] asociado al evento.
 */
fun getEventIcon(eventEs: String): ImageVector {
    return when (eventEs) {
        "Riego" -> Icons.Default.WaterDrop
        "Enfermedad" -> Icons.Default.BugReport
        "Fertilización" -> Icons.Default.Yard
        "Germinación" -> Icons.Default.Spa
        "Eliminación adventicias" -> Icons.Default.ContentCut
        "Entutorado" -> Icons.Default.Height
        else -> Icons.Default.EditNote
    }
}
