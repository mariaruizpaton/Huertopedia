package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

/**
 * Composable `actual` para iOS que proporciona un manejador para la vibración.
 *
 * Esta implementación utiliza `AudioServicesPlaySystemSound` con `kSystemSoundID_Vibrate` para
 * producir una vibración estándar del sistema.
 *
 * **Nota:** La API de iOS utilizada no permite personalizar la duración de la vibración,
 * por lo que el parámetro de duración es ignorado.
 *
 * @return Una función que toma una duración `Long` (ignorada) y activa la vibración del dispositivo.
 */
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