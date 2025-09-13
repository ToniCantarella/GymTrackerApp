package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun GymScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        modifier = modifier
    ) { innerPadding ->
        content(innerPadding)
    }
}