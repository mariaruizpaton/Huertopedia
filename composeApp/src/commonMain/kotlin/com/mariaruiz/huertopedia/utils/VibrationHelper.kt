/**
 * Este archivo define la funcionalidad esperada para controlar la vibración del dispositivo.
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

/**
 * Un Composable `expect` que proporciona una forma de controlar la vibración del dispositivo.
 *
 * Devuelve una función lambda que toma la duración de la vibración en milisegundos.
 * La implementación `actual` en cada plataforma se encargará de interactuar con el servicio
 * de vibración del sistema.
 *
 * @return Una función lambda `(Long) -> Unit` que puede ser invocada para hacer vibrar el dispositivo
 *         durante un tiempo específico.
 */
@Composable
expect fun rememberVibrationHandler(): (Long) -> Unit