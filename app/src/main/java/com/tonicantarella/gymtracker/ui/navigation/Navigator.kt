package com.tonicantarella.gymtracker.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

class Navigator {
    var navController: NavHostController? = null
    var isGuarded by mutableStateOf(false)
    var pendingAction: (() -> Unit)? = null

    fun registerNavController(navController: NavHostController) {
        this.navController = navController
    }

    fun guard (isGuarded: Boolean) {
        this.isGuarded = isGuarded
    }

    fun releaseGuard() {
        isGuarded = false
        pendingAction?.invoke()
        pendingAction = null
    }

    fun navigate(route: Route) {
        if (isGuarded) {
            pendingAction = { navigate(route) }
        } else {
            navController?.navigate(route) {
                popUpTo(route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    fun popBackStack() {
        if (isGuarded) {
            pendingAction = { popBackStack() }
        } else {
            navController?.popBackStack()
        }
    }
}