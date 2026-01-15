package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(isEnabled: Boolean = true, onBack: () -> Unit)