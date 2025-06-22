package com.example.gymtracker.ui.navigation

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@Composable
fun GymFloatingActionButton(
    navBackStackEntry: NavBackStackEntry?
) {
    navBackStackEntry?.let { entry ->
        val viewModel: GymFloatingActionButtonViewModel = viewModel(
            viewModelStoreOwner = entry,
            initializer = { GymFloatingActionButtonViewModel() }
        )
        if(viewModel.showFab){
            FloatingActionButton(
                onClick = viewModel.onClick,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                viewModel.content()
            }
        }
    }
}

@Composable
fun ProvideFloatingActionButton(
    onClick: () -> Unit,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    (viewModelStoreOwner as? NavBackStackEntry)?.let { owner ->
        val viewModel: GymFloatingActionButtonViewModel = viewModel(
            viewModelStoreOwner = owner,
            initializer = { GymFloatingActionButtonViewModel() },
        )
        LaunchedEffect(onClick, visible, content) {
            viewModel.showFab = visible

            viewModel.onClick = onClick
            viewModel.content = content
        }
    }
}