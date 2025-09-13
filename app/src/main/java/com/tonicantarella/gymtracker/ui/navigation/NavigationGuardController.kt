package com.tonicantarella.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class NavigationGuardController(
    private val showGuard: Boolean
) {
    var isGuarded by mutableStateOf(false)
    var guardDialogOpen by mutableStateOf(false)
    private var pendingNavigation: (() -> Unit)? = null

    fun guard(guarded: Boolean) {
        isGuarded = guarded && showGuard
    }

    fun navigate(action: () -> Unit) {
        if (isGuarded) {
            pendingNavigation = action
            guardDialogOpen = true
        } else {
            action()
        }
    }

    fun release() {
        pendingNavigation?.invoke()
        pendingNavigation = null
        guardDialogOpen = false
        isGuarded = false
    }

    fun dismissDialog() {
        guardDialogOpen = false
    }
}

@Composable
fun rememberNavigationGuard(showGuard: Boolean = false): NavigationGuardController {
    return remember(showGuard) { NavigationGuardController(showGuard) }
}