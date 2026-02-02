package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import dev.gitlive.firebase.storage.Data
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType

// ----------------------------------

/**
 * Lanzador real (`actual`) para el selector de imágenes en iOS.
 *
 * @param onLaunch La acción a ejecutar cuando se lanza el selector.
 */
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit,
) {
    /**
     * Inicia el proceso de selección de imágenes.
     */
    actual fun launch() {
        onLaunch()
    }
}

/**
 * Composable `actual` para iOS que proporciona un lanzador para el selector de imágenes de la galería.
 *
 * @param onImagePicked Callback que se invoca con los bytes de la imagen seleccionada o `null` si la operación fue cancelada.
 * @return Una instancia de [ImagePickerLauncher] que puede ser usada para iniciar el selector de imágenes.
 */
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    val rootViewController = getRootViewController()

    val imagePicker = remember {
        val picker = UIImagePickerController()
        // CORRECCIÓN: Usamos la constante global directa
        picker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false

        val delegate = ImagePickerDelegate(
            onImagePicked = { image ->
                val imageData = image?.toByteArray()
                onImagePicked(imageData)
                picker.dismissViewControllerAnimated(true, null)
            },
            onCancelled = {
                onImagePicked(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        )
        picker.delegate = delegate
        PickerData(picker, delegate)
    }

    return remember {
        ImagePickerLauncher(
            onLaunch = {
                rootViewController.presentViewController(imagePicker.picker, true, null)
            }
        )
    }
}

/**
 * Delegado para `UIImagePickerController` que maneja la selección de imágenes y la cancelación.
 *
 * @param onImagePicked Callback invocado cuando se selecciona una imagen.
 * @param onCancelled Callback invocado cuando el usuario cancela la selección.
 */
private class ImagePickerDelegate(
    private val onImagePicked: (UIImage?) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        // CORRECCIÓN: Usamos la constante en lugar del String hardcodeado
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        onImagePicked(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onCancelled()
    }
}

/**
 * Convierte un `UIImage` a un `ByteArray` en formato JPEG.
 *
 * @return Un `ByteArray` con los datos de la imagen, o `null` si la conversión falla.
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toByteArray(): ByteArray? {
    val data: NSData = UIImageJPEGRepresentation(this, 0.8) ?: return null
    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned {
        memcpy(it.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

/**
 * Contenedor de datos para el `UIImagePickerController` y su delegado.
 */
private data class PickerData(
    val picker: UIImagePickerController,
    val delegate: ImagePickerDelegate
)

/**
 * Obtiene el `UIViewController` raíz de la aplicación.
 *
 * @return El `UIViewController` raíz.
 * @throws IllegalStateException si no se encuentra la ventana clave o el controlador de vista raíz.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
private fun getRootViewController(): UIViewController {
    return kotlinx.cinterop.memScoped {
        val keyWindow = UIApplication.sharedApplication.keyWindow ?: throw IllegalStateException("No key window found")
        keyWindow.rootViewController ?: throw IllegalStateException("No root view controller found")
    }
}

/**
 * Convierte un `ByteArray` en un objeto `Data` de Firebase.
 *
 * @return Un objeto `Data` compatible con Firebase Storage.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toFirebaseData(): Data {
    val nsData = usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this.size.toULong()
        )
    }
    return Data(nsData)
}

/**
 * Composable `actual` para iOS que proporciona un lanzador para la cámara.
 *
 * @param onImageCaptured Callback que se invoca con los bytes de la imagen capturada o `null` si la operación fue cancelada.
 * @return Una instancia de [CameraLauncher] que puede ser usada para iniciar la cámara.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraLauncher(onImageCaptured: (ByteArray?) -> Unit): CameraLauncher {
    val rootViewController = getRootViewController() // Reutilizamos el helper para obtener el rootVC

    val imagePicker = remember {
        val picker = UIImagePickerController()
        // CORRECCIÓN: Aquí es donde tenías el error, ahora usamos la constante importada
        picker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.allowsEditing = false

        val delegate = ImagePickerDelegate( // Reutilizamos el mismo delegado que ya tenías
            onImagePicked = { image ->
                val imageData = image?.toByteArray()
                onImageCaptured(imageData)
                picker.dismissViewControllerAnimated(true, null)
            },
            onCancelled = {
                onImageCaptured(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        )

        picker.delegate = delegate
        PickerData(picker, delegate)
    }

    return remember {
        CameraLauncher {
            // Usamos el rootViewController para presentar, igual que en la galería
            rootViewController.presentViewController(imagePicker.picker, animated = true, completion = null)
        }
    }
}

/**
 * Lanzador real (`actual`) para la cámara en iOS.
 *
 * @param onLaunch La acción a ejecutar cuando se lanza la cámara.
 */
actual class CameraLauncher(private val onLaunch: () -> Unit) {
    /**
     * Inicia la captura de una imagen con la cámara.
     */
    actual fun capture() {
        onLaunch()
    }
}