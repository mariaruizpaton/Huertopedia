/**
 * Este archivo contiene las implementaciones reales (`actual`) para Android relacionadas con la
 * selección y captura de imágenes, incluyendo la corrección de la orientación de la imagen y
 * la conversión de datos para Firebase.
 */
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

/**
 * Implementación real (`actual`) para Android de la clase `ImagePickerLauncher`.
 */
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    /**
     * Ejecuta la acción de lanzamiento para abrir el selector de imágenes.
     */
    actual fun launch() {
        onLaunch()
    }
}

/**
 * Implementación real (`actual`) para Android de `rememberImagePicker`.
 *
 * @param onImagePicked Callback que se invoca con los bytes de la imagen seleccionada o `null`.
 * @return Una instancia de `ImagePickerLauncher`.
 */
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

/**
 * Implementación real (`actual`) para Android de `rememberCameraLauncher`.
 *
 * @param onImageCaptured Callback que se invoca con los bytes de la imagen capturada o `null`.
 * @return Una instancia de `CameraLauncher`.
 */
@Composable
actual fun rememberCameraLauncher(onImageCaptured: (ByteArray?) -> Unit): CameraLauncher {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
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

/**
 * Corrige la orientación de una imagen basándose en sus metadatos EXIF.
 *
 * @param context El contexto de la aplicación.
 * @param uri El URI de la imagen a corregir.
 * @return Un `ByteArray` con la imagen corregida, o `null` si ocurre un error.
 */
private fun fixImageOrientation(context: Context, uri: Uri): ByteArray? {
    try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null

        val originalBytes = inputStream.use { it.readBytes() }

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

        if (rotationInDegrees == 0f) {
            return originalBytes
        }

        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
        val matrix = Matrix()
        matrix.preRotate(rotationInDegrees)

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

        bitmap.recycle()
        if (rotatedBitmap != bitmap) rotatedBitmap.recycle()

        return outputStream.toByteArray()

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * Implementación real (`actual`) para Android de la clase `CameraLauncher`.
 */
actual class CameraLauncher(
    private val onLaunch: () -> Unit
) {
    /**
     * Ejecuta la acción de lanzamiento para abrir la cámara.
     */
    actual fun capture() {
        onLaunch()
    }
}

/**
 * Implementación real (`actual`) para Android de la función de extensión `toFirebaseData`.
 */
actual fun ByteArray.toFirebaseData(): Data {
    return Data(this)
}