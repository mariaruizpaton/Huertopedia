package com.mariaruiz.huertopedia.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.gitlive.firebase.storage.Data
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream

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

@Composable
actual fun rememberCameraLauncher(onImageCaptured: (ByteArray?) -> Unit): CameraLauncher {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            // AQUÍ ESTÁ EL CAMBIO: Llamamos a la función mágica que endereza la foto
            val fixedBytes = fixImageOrientation(context, tempUri!!)
            onImageCaptured(fixedBytes)
        } else {
            onImageCaptured(null)
        }
    }

    return remember {
        CameraLauncher(
            onLaunch = {
                try {
                    val file = File.createTempFile("cam_", ".jpg", context.externalCacheDir)
                    val authority = "${context.packageName}.provider"
                    val uri = FileProvider.getUriForFile(context, authority, file)
                    tempUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}

// --- FUNCIÓN MÁGICA PARA CORREGIR ROTACIÓN ---
private fun fixImageOrientation(context: Context, uri: Uri): ByteArray? {
    try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null

        // 1. Leemos los bytes originales
        val originalBytes = inputStream.use { it.readBytes() }

        // 2. Leemos la etiqueta EXIF para saber cuánto está girada
        // Abrimos un nuevo stream porque el anterior ya se leyó
        val exifStream = context.contentResolver.openInputStream(uri) ?: return originalBytes
        val exif = ExifInterface(exifStream)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        exifStream.close()

        val rotationInDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        // Si no está girada (0 grados), devolvemos los bytes tal cual (ahorramos memoria)
        if (rotationInDegrees == 0f) {
            return originalBytes
        }

        // 3. Si está girada, creamos un Bitmap y lo rotamos
        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
        val matrix = Matrix()
        matrix.preRotate(rotationInDegrees)

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // 4. Volvemos a convertir el Bitmap rotado a ByteArray (JPG)
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream) // Calidad 90%

        // Limpiamos memoria
        bitmap.recycle()
        if (rotatedBitmap != bitmap) rotatedBitmap.recycle()

        return outputStream.toByteArray()

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

actual class CameraLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun capture() {
        onLaunch()
    }
}

// --- AÑADE ESTA FUNCIÓN AL FINAL DEL ARCHIVO ---
actual fun ByteArray.toFirebaseData(): Data {
    return Data(this)
}