package com.tonicantarella.gymtracker.ui.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class NavigationDirection {
    FORWARD,
    BACK
}

class Navigator(
    private val scope: CoroutineScope
) {
    var navController: NavHostController? = null
    private val _navigationAttempts = MutableSharedFlow<NavigationDirection>()
    val navigationAttempts: SharedFlow<NavigationDirection> = _navigationAttempts
    var isGuarded: Boolean = false
    var pendingAction: (() -> Unit)? = null

    fun registerNavController(navController: NavHostController) {
        this.navController = navController
    }

    fun guard(isGuarded: Boolean) {
        this.isGuarded = isGuarded
    }

    fun releaseGuard() {
        isGuarded = false
        pendingAction?.invoke()
        pendingAction = null
    }

    fun navigate(route: Route) {
        scope.launch{
            _navigationAttempts.emit(NavigationDirection.FORWARD)
        }
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
        scope.launch{
            _navigationAttempts.emit(NavigationDirection.BACK)
        }
        if (isGuarded) {
            pendingAction = { popBackStack() }
        } else {
            navController?.popBackStack()
        }
    }
}