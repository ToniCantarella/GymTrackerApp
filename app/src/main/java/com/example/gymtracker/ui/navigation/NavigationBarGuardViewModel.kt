package com.example.gymtracker.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NavigationBarGuardViewModel : ViewModel() {
    var isGuarded by mutableStateOf(false)
    var onGuard by mutableStateOf({}, referentialEqualityPolicy())
    var onProceed by mutableStateOf({}, referentialEqualityPolicy())

    fun proceed() {
        onProceed()
    }
}