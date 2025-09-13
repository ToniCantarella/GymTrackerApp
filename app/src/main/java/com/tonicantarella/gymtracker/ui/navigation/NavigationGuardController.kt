package com.tonicantarella.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class NavigationGuardController {
    var isGuarded by mutableStateOf(false)
    var unsavedChangesDialogOpen by mutableStateOf(false)
    private var pendingNavigation: (() -> Unit)? = null

    fun guard(guarded: Boolean) {
        isGuarded = guarded
    }

    fun navigate(action: () -> Unit) {
        if (isGuarded) {
            pendingNavigation = action
            unsavedChangesDialogOpen = true
        } else {
            action()
        }
    }

    fun release() {
        pendingNavigation?.invoke()
        pendingNavigation = null
        unsavedChangesDialogOpen = false
        isGuarded = false
    }

    fun dismissDialog() {
        unsavedChangesDialogOpen = false
    }
}

@Composable
fun rememberNavigationGuard(): NavigationGuardController {
    return remember { NavigationGuardController() }
}