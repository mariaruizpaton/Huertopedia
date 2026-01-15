package com.mariaruiz.huertopedia.utils

import androidx.activity.compose.BackHandler as AndroidBackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled = isEnabled, onBack = onBack)
}