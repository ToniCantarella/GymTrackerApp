package com.example.gymtracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTopAppBar(
    navBackStackEntry: NavBackStackEntry?
) {
    navBackStackEntry?.let { entry ->
        val viewModel: GymTopAppBarViewModel = viewModel(
            viewModelStoreOwner = entry,
            initializer = { GymTopAppBarViewModel() }
        )
        var debouncedShowTopBar by remember { mutableStateOf(true) }

        LaunchedEffect(viewModel.showTopBar) {
            delay(50)
            debouncedShowTopBar = viewModel.showTopBar
        }

        AnimatedVisibility(
            visible = debouncedShowTopBar,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ){
            TopAppBar(
                title = viewModel.title,
                navigationIcon = viewModel.navigationIcon,
                actions = viewModel.actions
            )
        }
    }
}

@Composable
fun ProvideTopAppBar(
    title: @Composable () -> Unit = {},
    navigationItem: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    (viewModelStoreOwner as? NavBackStackEntry)?.let { owner ->
        val viewModel: GymTopAppBarViewModel = viewModel(
            viewModelStoreOwner = owner,
            initializer = { GymTopAppBarViewModel() },
        )
        LaunchedEffect(title, navigationItem, actions) {
            viewModel.showTopBar = true
            viewModel.title = title
            viewModel.navigationIcon = navigationItem
            viewModel.actions = actions
        }
    }
}