package com.example.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@Composable
fun ProvideNavigationBarGuard(
    isGuarded: Boolean = true,
    onGuard: () -> Unit
) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    (viewModelStoreOwner as? NavBackStackEntry)?.let { owner ->
        val viewModel: NavigationBarGuardViewModel = viewModel(
            viewModelStoreOwner = owner,
            initializer = { NavigationBarGuardViewModel() },
        )
        LaunchedEffect(isGuarded, onGuard) {
            viewModel.isGuarded = isGuarded
            viewModel.onGuard = onGuard
        }
    }
}

@Composable
fun rememberProceedOnGuardCleared(): () -> Unit {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val viewModel: NavigationBarGuardViewModel? = (viewModelStoreOwner as? NavBackStackEntry)?.let {
        viewModel(viewModelStoreOwner = it)
    }

    return remember(viewModel) {
        {
            viewModel?.proceed()
        }
    }
}