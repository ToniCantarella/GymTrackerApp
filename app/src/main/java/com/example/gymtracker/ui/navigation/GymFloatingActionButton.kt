package com.example.gymtracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GymFloatingActionButton(
    navBackStackEntry: NavBackStackEntry?
) {
    navBackStackEntry?.let { entry ->
        val viewModel: GymFloatingActionButtonViewModel = viewModel(
            viewModelStoreOwner = entry,
            initializer = { GymFloatingActionButtonViewModel() }
        )

        val imeBottom = WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding()
        val fabOffset = imeBottom / 3
        val fabPadding = (imeBottom - fabOffset).coerceAtLeast(0.dp)

        AnimatedVisibility(
            visible = viewModel.showFab,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FloatingActionButton(
                onClick = { if (viewModel.enabled) viewModel.onClick() },
                contentColor = if(viewModel.enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = .5f),
                containerColor = if (viewModel.enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .padding(bottom = fabPadding)
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
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    (viewModelStoreOwner as? NavBackStackEntry)?.let { owner ->
        val viewModel: GymFloatingActionButtonViewModel = viewModel(
            viewModelStoreOwner = owner,
            initializer = { GymFloatingActionButtonViewModel() },
        )
        LaunchedEffect(onClick, visible, enabled, content) {
            viewModel.showFab = visible
            viewModel.enabled = enabled
            viewModel.onClick = onClick
            viewModel.content = content
        }
    }
}