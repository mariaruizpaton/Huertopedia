/**
 * Este archivo define las funcionalidades esperadas para la selección de imágenes y la captura con la cámara,
 * así como utilidades para la conversión de datos de imagen para Firebase.
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.storage.Data

/**
 * Un Composable `expect` que proporciona un lanzador para seleccionar una imagen de la galería del dispositivo.
 *
 * @param onImagePicked Una función lambda que se invoca con el `ByteArray` de la imagen seleccionada,
 *                      o `null` si la operación fue cancelada.
 * @return Una instancia de [ImagePickerLauncher] que puede ser usada para iniciar el selector de imágenes.
 */
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher

/**
 * Clase `expect` que representa un lanzador para el selector de imágenes de la galería.
 */
expect class ImagePickerLauncher {
    /**
     * Abre el selector de imágenes de la galería del dispositivo.
     */
    fun launch()
}

/**
 * Un Composable `expect` que proporciona un lanzador para capturar una imagen usando la cámara del dispositivo.
 *
 * @param onImageCaptured Una función lambda que se invoca con el `ByteArray` de la imagen capturada,
 *                        o `null` si la operación fue cancelada.
 * @return Una instancia de [CameraLauncher] que puede ser usada para iniciar la cámara.
 */
@Composable
expect fun rememberCameraLauncher(onImageCaptured: (ByteArray?) -> Unit): CameraLauncher

/**
 * Clase `expect` que representa un lanzador para la aplicación de cámara.
 */
expect class CameraLauncher {
    /**
     * Abre la interfaz de la cámara para capturar una foto.
     */
    fun capture()
}

/**
 * Función de extensión `expect` para convertir un `ByteArray` en un objeto `Data` compatible con Firebase Storage.
 *
 * La implementación `actual` en cada plataforma se encargará de la conversión específica.
 *
 * @return Una instancia de `Data` lista para ser subida a Firebase Storage.
 */
expect fun ByteArray.toFirebaseData(): Data