package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

@Composable
actual fun rememberVibrationHandler(): (Long) -> Unit {
    return remember {
        { _ ->
            // En iOS usamos la vibración estándar del sistema (kSystemSoundID_Vibrate = 4095)
            // La duración no es personalizable fácilmente con esta API, así que ignoramos el parámetro.
            AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        }
    }
}