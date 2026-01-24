package com.mariaruiz.huertopedia.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.utils.rememberImagePicker
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import com.mariaruiz.huertopedia.repositories.ThemeRepository
import com.mariaruiz.huertopedia.components.LanguageButton
import com.mariaruiz.huertopedia.i18n.LocalStrings
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import io.kamel.core.Resource
import kotlinx.coroutines.launch
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
// ------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel,
    languageRepository: LanguageRepository,
    themeRepository: ThemeRepository
) {
    val strings = LocalStrings.current
    val themePref by themeRepository.themePreference.collectAsState()
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var tempNombre by remember { mutableStateOf(viewModel.name ?: "") }
    var tempDesc by remember { mutableStateOf(viewModel.descripcion) }
    var tempImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // 1. CONFIGURACIÓN DE PERMISOS
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val imagePicker = rememberImagePicker { bytes ->
        tempImageBytes = bytes
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            tempNombre = viewModel.name ?: ""
            tempDesc = viewModel.descripcion
        } else {
            tempImageBytes = null
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
                title = { Text(strings.profileTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBack)
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // FOTO DE PERFIL
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        // 2. MODIFICAMOS EL CLICKABLE CON PERMISOS
                        .clickable(enabled = isEditing) {
                            scope.launch {
                                try {
                                    // 1. Intentamos ser educados y pedir permiso (necesario en iOS y Android viejos)
                                    controller.providePermission(Permission.GALLERY)

                                    // 2. Si dicen que sí, abrimos
                                    imagePicker.launch()

                                } catch (e: DeniedAlwaysException) {
                                    // 3. CASO FALSO NEGATIVO (Android 13/14)
                                    // El sistema dice "No te doy permiso general", pero nos deja usar el picker.
                                    println("Permiso denegado permanentemente. Intentando abrir galería de todas formas (Lógica Android 14)...")

                                    try {
                                        // Intentamos abrir sin permiso
                                        imagePicker.launch()
                                    } catch (e: Exception) {
                                        // Si falla aquí, es que DE VERDAD no nos dejan. Abrimos ajustes.
                                        println("Fallo total. Abriendo ajustes.")
                                        controller.openAppSettings()
                                    }

                                } catch (e: DeniedException) {
                                    // 4. El usuario dijo explícitamente "NO" en el popup ahora mismo.
                                    // Aquí respetamos su decisión y no hacemos nada.
                                    println("El usuario ha rechazado el permiso puntualmente.")

                                } catch (e: Exception) {
                                    // 5. Cualquier otro error raro, intentamos abrir por si acaso.
                                    e.printStackTrace()
                                    imagePicker.launch()
                                }
                            }
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val imageResource: Resource<Painter> = when {
                        tempImageBytes != null -> asyncPainterResource(data = tempImageBytes!!)
                        !viewModel.imagenUrlRenderizable.isNullOrEmpty() -> asyncPainterResource(data = viewModel.imagenUrlRenderizable!!)
                        else -> asyncPainterResource(data = Unit) // Placeholder or error
                    }


                    if (tempImageBytes != null || !viewModel.imagenUrlRenderizable.isNullOrEmpty()) {
                        KamelImage(
                            resource = imageResource,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            animationSpec = tween(500)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Icono de la cámara pequeño (visual)
                if (isEditing) {
                    Surface(
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Filled.CameraAlt, null, Modifier.padding(6.dp), Color.White)
                    }
                }
            }

            // ... resto del código igual (text fields, botones, etc) ...

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = { Text(strings.loginName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = tempDesc,
                    onValueChange = { tempDesc = it },
                    label = { Text(strings.cropLogObservations) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                        Text(strings.gardenCancel)
                    }
                    Button(
                        onClick = {
                            if (tempImageBytes != null) {
                                viewModel.uploadImageBytes(tempImageBytes!!) {
                                    viewModel.updateUserData(tempNombre, tempDesc, viewModel.imagenUrl)
                                }
                            } else {
                                viewModel.updateUserData(tempNombre, tempDesc, viewModel.imagenUrl)
                            }
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.gardenSave)
                    }
                }
            } else {
                // ... (Bloque de visualización que ya tenías) ...

                // TARJETA DE INFORMACIÓN CENTRADA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.name ?: "Usuario",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.descripcion.ifEmpty { strings.welcomeSubtitle },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HorizontalDivider(Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // AJUSTES
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(strings.changeLanguage, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    LanguageButton(languageRepository)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(strings.themeTitle, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Row {
                        IconButton(onClick = { scope.launch { themeRepository.setTheme("light") } }) {
                            Icon(Icons.Default.LightMode, strings.themeLight, tint = if(themePref == "light") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { scope.launch { themeRepository.setTheme("dark") } }) {
                            Icon(Icons.Default.DarkMode, strings.themeDark, tint = if(themePref == "dark") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { scope.launch { themeRepository.setTheme("system") } }) {
                            Icon(Icons.Default.SettingsBrightness, strings.themeSystem, tint = if(themePref == "system") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(strings.logoutButton, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}