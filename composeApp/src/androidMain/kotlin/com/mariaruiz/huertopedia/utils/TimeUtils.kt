/**
 * Implementación real (`actual`) para Android de la función `getCurrentTimeMillis`.
 */
package com.mariaruiz.huertopedia.utils

/**
 * Devuelve el tiempo actual del sistema en milisegundos.
 *
 * Utiliza la función `System.currentTimeMillis()` de Java, que está disponible en Android.
 *
 * @return El tiempo actual en milisegundos desde la época (epoch).
 */
actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}