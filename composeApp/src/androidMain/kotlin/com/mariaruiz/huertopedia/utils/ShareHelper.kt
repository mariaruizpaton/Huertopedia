package com.mariaruiz.huertopedia.utils

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareHandler(): (String) -> Unit {
    val context = LocalContext.current
    return remember {
        { text ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Compartir mi jardinera")
            context.startActivity(shareIntent)
        }
    }
}