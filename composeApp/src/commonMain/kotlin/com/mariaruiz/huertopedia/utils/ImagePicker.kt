package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.storage.Data // Importante añadir este import

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher

expect class ImagePickerLauncher {
    fun launch()
}

// Añadimos esta línea: es la promesa de que cada plataforma
// sabrá convertir los bytes al formato de Firebase
expect fun ByteArray.toFirebaseData(): Data
