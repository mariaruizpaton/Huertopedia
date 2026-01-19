package com.mariaruiz.huertopedia.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.gitlive.firebase.storage.Data

actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            val imageBytes = uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.readBytes()
                }
            }
            onImagePicked(imageBytes)
        }
    )

    return remember {
        ImagePickerLauncher {
            imagePickerLauncher.launch("image/*")
        }
    }
}

// --- AÑADE ESTA FUNCIÓN AL FINAL DEL ARCHIVO ---
actual fun ByteArray.toFirebaseData(): Data {
    return Data(this)
}