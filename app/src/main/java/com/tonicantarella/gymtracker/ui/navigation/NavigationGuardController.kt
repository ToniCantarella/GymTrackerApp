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
    private var pendingNavigation: (() -> Unit)? = null

    fun guard(guarded: Boolean) {
        isGuarded = guarded && showGuard
    }

    fun navigate(onGuarded:() -> Unit, navigationAction: () -> Unit) {
        if (isGuarded) {
            pendingNavigation = navigationAction
            onGuarded()
        } else {
            navigationAction()
        }
    }

    fun release() {
        pendingNavigation?.invoke()
        pendingNavigation = null
        isGuarded = false
    }
}

@Composable
fun rememberNavigationGuard(showGuard: Boolean = false): NavigationGuardController {
    return remember(showGuard) { NavigationGuardController(showGuard) }
}