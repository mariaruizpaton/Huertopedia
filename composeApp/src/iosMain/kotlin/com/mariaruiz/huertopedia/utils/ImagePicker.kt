package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import dev.gitlive.firebase.storage.Data


// Clase contenedora para la lógica del lanzador.
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    // Obtenemos una referencia al UIViewController actual en Compose
    val rootViewController = getRootViewController()

    // Creamos y recordamos el lanzador del selector de imágenes.
    val imagePicker = remember {
        // Se crea una instancia del selector de imágenes de UIKit.
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false // O true si quieres que el usuario pueda recortar la imagen.

        // El 'delegate' se encarga de recibir los eventos del selector.
        val delegate = ImagePickerDelegate(
            onImagePicked = { image ->
                // Cuando se selecciona una imagen, se convierte a ByteArray y se pasa al callback.
                val imageData = image?.toByteArray()
                onImagePicked(imageData)
                // Cerramos el selector.
                picker.dismissViewControllerAnimated(true, null)
            },
            onCancelled = {
                // Si el usuario cancela, se informa con null y se cierra el selector.
                onImagePicked(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        )
        // Guardamos el delegate en una propiedad para que no sea recolectado por el garbage collector.
        picker.delegate = delegate
        PickerData(picker, delegate) // Contenedor para el picker y su delegate.
    }

    // Devolvemos nuestro lanzador que presentará el selector de imágenes de UIKit.
    return remember {
        ImagePickerLauncher(
            onLaunch = {
                rootViewController.presentViewController(imagePicker.picker, true, null)
            }
        )
    }
}

// Clase interna para gestionar el ciclo de vida del delegate.
private class ImagePickerDelegate(
    private val onImagePicked: (UIImage?) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        // Se obtiene la imagen original del diccionario de información.
        val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage
        onImagePicked(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onCancelled()
    }
}

// Función auxiliar para convertir un UIImage a ByteArray.
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toByteArray(): ByteArray? {
    // Usamos el método nativo para convertir la imagen a un formato de datos (NSData).
    val data: NSData = platform.UIKit.UIImageJPEGRepresentation(this, 0.8) ?: return null
    // Creamos un array de bytes en Kotlin del tamaño adecuado.
    val bytes = ByteArray(data.length.toInt())
    // Copiamos los bytes desde NSData al ByteArray de Kotlin.
    bytes.usePinned {
        memcpy(it.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

// Contenedor de datos para evitar que el delegate sea eliminado prematuramente.
private data class PickerData(
    val picker: UIImagePickerController,
    val delegate: ImagePickerDelegate
)

// Función auxiliar para obtener el UIViewController raíz.
@OptIn(ExperimentalForeignApi::class)
@Composable
private fun getRootViewController(): UIViewController {
    // Este método busca el UIViewController raíz de la ventana actual.
    // Es una forma estándar de acceder al contexto de la vista desde Compose en iOS.
    return kotlinx.cinterop.memScoped {
        val keyWindow = platform.UIKit.UIApplication.sharedApplication.keyWindow ?: throw IllegalStateException("No key window found")
        keyWindow.rootViewController ?: throw IllegalStateException("No root view controller found")
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.toFirebaseData(): Data {
    // 1. Convertimos el ByteArray de Kotlin a NSData de iOS
    val nsData = usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this.size.toULong()
        )
    }
    // 2. Envolvemos el NSData en el objeto Data de la librería GitLive
    return Data(nsData)
}

