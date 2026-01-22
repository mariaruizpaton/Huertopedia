package com.mariaruiz.huertopedia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.model.Planter
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.GardenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropLogScreen(
    planter: Planter,
    onBack: () -> Unit,
    gardenViewModel: GardenViewModel
) {
    val flowerpots by gardenViewModel.getFlowerpots(planter.id).collectAsState(initial = emptyList())

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
        }
    ) { padding ->
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
                            Text("${pot.tipoAccion} el ${formatDate(pot.fechaSiembra)}")
                        }
                    )
                }
            }

            if (flowerpots.isEmpty()) {
                item {
                    Text("No hay actividades registradas en esta jardinera.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

// Función auxiliar simple para fechas (puedes mejorarla luego)
fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "Fecha desconocida"
    // Aquí podrías usar una librería de fechas, de momento devolvemos algo básico
    return "recientemente"
}