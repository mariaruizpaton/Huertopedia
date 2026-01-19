package com.mariaruiz.huertopedia.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.utils.rememberImagePicker
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel,
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempNombre by remember { mutableStateOf(viewModel.name ?: "") }
    var tempDesc by remember { mutableStateOf(viewModel.descripcion) }

    val imagePicker = rememberImagePicker { bytes ->
        bytes?.let { viewModel.uploadImageBytes(it) }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            tempNombre = viewModel.name ?: ""
            tempDesc = viewModel.descripcion
        }
    }

    LaunchedEffect(viewModel.imagenUrl) {
        if (viewModel.imagenUrl.isNotEmpty()) {
            viewModel.obtenerUrlDescarga()
        }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN DE IMAGEN DE PERFIL
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable(enabled = isEditing) { imagePicker.launch() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    val path = viewModel.imagenUrlRenderizable

                    if (!path.isNullOrEmpty()) {
                        KamelImage(
                            resource = asyncPainterResource(data = path),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            // Usar un crossfade hace que el cambio no sea brusco
                            animationSpec = tween(500)
                        )
                    } else {
                        // ESTO SE MOSTRARÁ POR DEFECTO
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Imagen predeterminada",
                                modifier = Modifier.padding(30.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Indicador visual de "Cámara" solo cuando se edita
                if (isEditing) {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FORMULARIO O INFORMACIÓN
            if (isEditing) {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = tempDesc,
                    onValueChange = { tempDesc = it },
                    label = { Text("Acerca de mí") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            viewModel.updateUserData(tempNombre, tempDesc, viewModel.imagenUrl)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Guardar")
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.name ?: "Usuario",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.descripcion.ifEmpty { "Añade una descripción sobre tu huerto..." },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}