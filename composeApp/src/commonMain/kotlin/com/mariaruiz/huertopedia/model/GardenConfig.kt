package com.mariaruiz.huertopedia.model

/**
 * Objeto que contiene la configuración para las dimensiones del jardín.
 * Define los límites para el número de filas y columnas.
 */
object GardenConfig {
    /** El número mínimo de filas permitido en una jardinera. */
    const val MIN_ROWS = 1
    /** El número máximo de filas permitido en una jardinera. */
    const val MAX_ROWS = 2 // <--- RESTRINGIDO A 2
    /** El número mínimo de columnas permitido en una jardinera. */
    const val MIN_COLS = 1
    /** El número máximo de columnas permitido en una jardinera. */
    const val MAX_COLS = 8 // <--- AUMENTADO A 8
}
