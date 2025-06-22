package com.example.gymtracker.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GymTopAppBarViewModel: ViewModel() {
    var showTopBar by mutableStateOf(false)

    var title by mutableStateOf<@Composable () -> Unit>({ }, referentialEqualityPolicy())

    var navigationIcon by mutableStateOf<@Composable () -> Unit>({ }, referentialEqualityPolicy())

    var actions by mutableStateOf<@Composable RowScope.() -> Unit>({ }, referentialEqualityPolicy())
}