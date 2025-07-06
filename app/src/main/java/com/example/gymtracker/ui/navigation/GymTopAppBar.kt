package com.example.gymtracker.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTopAppBar(
    navBackStackEntry: NavBackStackEntry?
) {
    navBackStackEntry?.let { entry ->
        val viewModel: GymTopAppBarViewModel = viewModel(
            viewModelStoreOwner = entry,
            initializer = { GymTopAppBarViewModel()}
        )

        if (viewModel.showTopBar){
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
    title: @Composable ()-> Unit = {},
    navigationItem: @Composable ()-> Unit = {},
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