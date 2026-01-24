package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.storage.Data

// --- ESTO YA LO TIENES (Para Galería) ---
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher

expect class ImagePickerLauncher {
    fun launch()
}

// --- AÑADE ESTO NUEVO (Para Cámara) ---
@Composable
expect fun rememberCameraLauncher(onImageCaptured: (ByteArray?) -> Unit): CameraLauncher

expect class CameraLauncher {
    fun capture()
}

expect fun ByteArray.toFirebaseData(): Data