package com.example.gymtracker.ui.workouts.split

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplitScreen(
    viewModel: SplitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SplitScreen(
        splitName = uiState.name
    )
}

@Composable
fun SplitScreen(
    splitName: String
) {
    Text(splitName)
}