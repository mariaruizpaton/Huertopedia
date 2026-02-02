/**
 * Este archivo contiene funciones de utilidad relacionadas con el tiempo.
 */
package com.mariaruiz.huertopedia.utils

/**
 * Función `expect` que se implementará en cada plataforma para obtener el tiempo actual del sistema en milisegundos.
 *
 * Esta declaración define una API común para el código `commonMain`. La implementación real (`actual`)
 * en cada módulo de plataforma proporcionará el método específico de esa plataforma para obtener el
 * timestamp actual.
 *
 * @return El tiempo actual en milisegundos desde la época (epoch).
 */
expect fun getCurrentTimeMillis(): Long