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

/**
 * Composable para la pantalla de perfil y configuración del usuario.
 *
 * Permite al usuario ver y editar su información de perfil (nombre, descripción, imagen),
 * cambiar el idioma y el tema de la aplicación, y cerrar sesión.
 * La pantalla tiene dos modos: visualización y edición.
 *
 * @param onLogout Callback para gestionar el cierre de sesión.
 * @param onBack Callback para navegar hacia atrás.
 * @param viewModel Instancia de [LoginViewModel] para gestionar los datos y el estado del usuario.
 * @param languageRepository Repositorio para gestionar la preferencia de idioma.
 * @param themeRepository Repositorio para gestionar la preferencia de tema.
 */
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

    // --- Estado para el modo de edición ---
    var isEditing by remember { mutableStateOf(false) }
    var tempNombre by remember { mutableStateOf(viewModel.name ?: "") }
    var tempDesc by remember { mutableStateOf(viewModel.descripcion) }
    var tempImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // --- Gestión de permisos para la galería ---
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val imagePicker = rememberImagePicker { bytes ->
        tempImageBytes = bytes
    }

    // Sincroniza los datos temporales cuando se entra o sale del modo de edición
    LaunchedEffect(isEditing) {
        if (isEditing) {
            tempNombre = viewModel.name ?: ""
            tempDesc = viewModel.descripcion
        } else {
            tempImageBytes = null // Limpia la imagen temporal al salir de edición
        }
    }

    // Obtiene la URL de descarga de la imagen cuando cambia
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
                            Icon(Icons.Filled.Edit, contentDescription = "Editar perfil")
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

            // --- Imagen de perfil ---
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(enabled = isEditing) {
                            scope.launch {
                                try {
                                    controller.providePermission(Permission.GALLERY)
                                    imagePicker.launch()
                                } catch (e: DeniedAlwaysException) {
                                    controller.openAppSettings()
                                } catch (e: DeniedException) {
                                    println("Permiso a galería denegado")
                                } catch (e: Exception) {
                                    imagePicker.launch() // Intento para plataformas sin sistema de permisos complejo
                                }
                            }
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val imageResource: Resource<Painter> = when {
                        tempImageBytes != null -> asyncPainterResource(data = tempImageBytes!!)
                        !viewModel.imagenUrlRenderizable.isNullOrEmpty() -> asyncPainterResource(data = viewModel.imagenUrlRenderizable!!)
                        else -> asyncPainterResource(data = Unit) // Placeholder
                    }

                    if (tempImageBytes != null || !viewModel.imagenUrlRenderizable.isNullOrEmpty()) {
                        KamelImage(
                            resource = imageResource,
                            contentDescription = "Imagen de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            animationSpec = tween(500)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder de perfil",
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isEditing) {
                    Surface(
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Filled.CameraAlt, "Cambiar imagen", Modifier.padding(6.dp), Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Contenido: Modo Edición vs. Modo Visualización ---
            if (isEditing) {
                // --- Formulario de Edición ---
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
                    label = { Text(strings.profileAboutMe) },
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
                                viewModel.uploadImageBytes(tempImageBytes!!)
                            }
                            viewModel.updateUserData(tempNombre, tempDesc, viewModel.imagenUrl)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.gardenSave)
                    }
                }
            } else {
                // --- Vista de Perfil y Configuración ---
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

                // --- Controles de Configuración ---
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

            // --- Botón de Cerrar Sesión ---
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
