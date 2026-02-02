/**
 * Implementación real (`actual`) para Android de la función `rememberVibrationHandler`.
 *
 * Esta función crea y recuerda una lambda que, al ser invocada con una duración en milisegundos,
 * hace vibrar el dispositivo. Utiliza las API de vibración de Android, manejando la compatibilidad
 * entre diferentes versiones del sistema operativo.
 *
 * @return Una función `(Long) -> Unit` que provoca la vibración del dispositivo.
 */
package com.mariaruiz.huertopedia.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberVibrationHandler(): (Long) -> Unit {
    val context = LocalContext.current

    return remember {
        { milliseconds ->
            // Se obtiene el servicio de vibración del sistema.
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Se comprueba si el dispositivo tiene capacidad de vibración.
            if (vibrator.hasVibrator()) {
                // Se hace vibrar el dispositivo con la duración especificada.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(milliseconds)
                }
            }
        }
    }
}