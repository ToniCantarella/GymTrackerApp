package com.example.gymtracker.ui.workouts.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SplitUiState(
    val name: String = ""
)

class SplitViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val split = savedStateHandle.toRoute<Route.Split>()

    private val _uiState = MutableStateFlow(SplitUiState(name = split.name))
    val uiState = _uiState.asStateFlow()
}