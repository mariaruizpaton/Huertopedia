package com.mariaruiz.huertopedia.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.mariaruiz.huertopedia.utils.BackHandler
import com.mariaruiz.huertopedia.viewmodel.LoginViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel,
) {
    BackHandler {
        onBack()
    }
}