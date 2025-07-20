package com.example.gymtracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.gymtracker.R
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
        ) {
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

@Composable
fun TopBarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxSize: Int = Int.MAX_VALUE
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxSize) {
                onValueChange(it)
            }
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.name)
            )
        },
        interactionSource = interactionSource,
        trailingIcon = {
            if (isFocused && value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.clear)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}