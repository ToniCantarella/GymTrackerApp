package com.example.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GymFloatingActionButtonViewModel : ViewModel() {
    var showFab by mutableStateOf(false)
    var enabled by mutableStateOf(true)

    var onClick by mutableStateOf({}, referentialEqualityPolicy())
    var content by mutableStateOf<@Composable () -> Unit>({ }, referentialEqualityPolicy())
}