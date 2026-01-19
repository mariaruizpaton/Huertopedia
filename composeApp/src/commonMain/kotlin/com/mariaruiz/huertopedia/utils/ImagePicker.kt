package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher

expect class ImagePickerLauncher {
    fun launch()
}
