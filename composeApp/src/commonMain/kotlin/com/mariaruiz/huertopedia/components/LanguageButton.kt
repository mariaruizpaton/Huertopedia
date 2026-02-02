package com.mariaruiz.huertopedia.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.*
import com.mariaruiz.huertopedia.i18n.LocalStrings
import com.mariaruiz.huertopedia.repositories.LanguageRepository
import kotlinx.coroutines.launch

/**
 * Un botón que permite al usuario cambiar el idioma de la aplicación.
 *
 * Al hacer clic, cambia entre español ("es") e inglés ("en").
 * Muestra un icono de traducción y un texto que indica la acción.
 *
 * @param repository El repositorio utilizado para obtener y establecer el idioma actual.
 */
@Composable
fun LanguageButton(repository: LanguageRepository) {
    val currentLang by repository.currentLanguage.collectAsState()
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                val nextLang = if (currentLang == "es") "en" else "es"
                repository.setLanguage(nextLang)
            }
        }
    ) {
        Icon(Icons.Default.Translate, contentDescription = null)
        Text(text = strings.changeLanguage)
    }
}
