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

actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}

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

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toByteArray(): ByteArray? {
    val data: NSData = UIImageJPEGRepresentation(this, 0.8) ?: return null
    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned {
        memcpy(it.addressOf(0), data.bytes, data.length)
    }
    return bytes
}

private data class PickerData(
    val picker: UIImagePickerController,
    val delegate: ImagePickerDelegate
)

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun getRootViewController(): UIViewController {
    return kotlinx.cinterop.memScoped {
        val keyWindow = UIApplication.sharedApplication.keyWindow ?: throw IllegalStateException("No key window found")
        keyWindow.rootViewController ?: throw IllegalStateException("No root view controller found")
    }
}

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

actual class CameraLauncher(private val onLaunch: () -> Unit) {
    actual fun capture() {
        onLaunch()
    }
}