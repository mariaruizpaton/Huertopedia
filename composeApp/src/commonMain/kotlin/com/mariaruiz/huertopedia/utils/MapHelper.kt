/**
 * Este archivo define la funcionalidad esperada para interactuar con mapas en cada plataforma.
 */
package com.mariaruiz.huertopedia.utils

import androidx.compose.runtime.Composable

/**
 * Una función Composable que se espera que sea implementada en cada plataforma (Android, iOS).
 *
 * Esta función proporciona una forma de abrir una aplicación de mapas y buscar una ubicación
 * o término específico. Devuelve una función lambda que toma un [String] de consulta
 * (por ejemplo, "viveros") y no devuelve nada.
 *
 * La implementación real (`actual`) en cada módulo de plataforma se encargará de
 * la lógica específica para abrir la aplicación de mapas correspondiente (Google Maps en Android,
 * Apple Maps en iOS).
 *
 * @return Una función lambda `(String) -> Unit` que puede ser invocada para iniciar una búsqueda en el mapa.
 */
@Composable
expect fun rememberMapHandler(): (String) -> Unit