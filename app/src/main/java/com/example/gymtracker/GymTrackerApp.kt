package com.example.gymtracker

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.gymtracker.ui.cardio.cardioworkout.CardioWorkoutScreen
import com.example.gymtracker.ui.cardio.cardioworkouts.CardioWorkoutsScreen
import com.example.gymtracker.ui.cardio.createcardioworkout.CreateCardioWorkoutScreen
import com.example.gymtracker.ui.common.UnsavedChangesDialog
import com.example.gymtracker.ui.gym.creategymworkout.CreateGymWorkoutScreen
import com.example.gymtracker.ui.gym.gymworkout.GymWorkoutScreen
import com.example.gymtracker.ui.gym.gymworkouts.GymWorkoutsScreen
import com.example.gymtracker.ui.info.InfoScreen
import com.example.gymtracker.ui.navigation.GymScaffold
import com.example.gymtracker.ui.navigation.NavigationBarRoute
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.stats.cardio.CardioWorkoutSessionScreen
import com.example.gymtracker.ui.stats.cardio.CardioWorkoutStatsScreen
import com.example.gymtracker.ui.stats.gym.GymWorkoutSessionStatsScreen
import com.example.gymtracker.ui.stats.gym.GymWorkoutStatsScreen
import com.example.gymtracker.ui.stats.overview.StatsOverviewScreen
import com.example.gymtracker.ui.welcome.WelcomeScreen

@Composable
fun GymTrackerApp(
    viewModel: MainViewModel
) {
    val mainUiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    val navigationAnimationMoveInt = 1500
    val enter = if (isLandscape) fadeIn() else slideInHorizontally { navigationAnimationMoveInt }
    val exit = if (isLandscape) fadeOut() else slideOutHorizontally { -navigationAnimationMoveInt }
    val popEnter =
        if (isLandscape)
            fadeIn()
        else
            slideInHorizontally { -navigationAnimationMoveInt }
    val popExit =
        if (isLandscape)
            fadeOut()
        else
            slideOutHorizontally { navigationAnimationMoveInt }

    val navigationBarRoutes = listOf(
        NavigationBarRoute(
            titleResInt = R.string.gym,
            route = Route.GymMain,
            iconResInt = R.drawable.weight
        ),
        NavigationBarRoute(
            titleResInt = R.string.cardio,
            route = Route.CardioMain,
            iconResInt = R.drawable.run
        ),
        NavigationBarRoute(
            titleResInt = R.string.stats,
            route = Route.StatsMain,
            iconResInt = R.drawable.stats
        ),
        NavigationBarRoute(
            titleResInt = R.string.info,
            route = Route.Info,
            iconResInt = R.drawable.info
        )
    )

    var isNavigationGuarded by remember { mutableStateOf(false) }
    var unsavedChangesDialogOpen by remember { mutableStateOf(false) }
    var pendingNavigationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun onNavigationGuardChange(guard: Boolean) {
        isNavigationGuarded = guard
    }

    fun releaseNavigationGuard() {
        pendingNavigationAction?.invoke()
        pendingNavigationAction = null
        unsavedChangesDialogOpen = false
        isNavigationGuarded = false
    }

    fun navigateGuarded(navigationAction: () -> Unit) {
        if (isNavigationGuarded) {
            pendingNavigationAction = navigationAction
            unsavedChangesDialogOpen = true
        } else {
            navigationAction()
            pendingNavigationAction = null
            unsavedChangesDialogOpen = false
        }
    }

    fun navigate(route: Route) {
        navigateGuarded {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    fun popBackStack() {
        navigateGuarded {
            navController.popBackStack()
        }
    }

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }

    GymScaffold(
        navController = navController,
        isLandscape = isLandscape,
        navigate = ::navigate,
        navigationBarRoutes = navigationBarRoutes
    ) { innerPadding ->
        NavHost(
            enterTransition = { enter },
            exitTransition = { exit },
            popEnterTransition = { popEnter },
            popExitTransition = { popExit },
            navController = navController,
            startDestination = mainUiState.initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Welcome> {
                WelcomeScreen(
                    onUnderstoodClick = {
                        viewModel.onUserWelcomed()
                        navigate(Route.GymMain)
                    }
                )
            }

            navigation<Route.GymMain>(startDestination = Route.GymWorkouts) {
                composable<Route.GymWorkouts> {
                    GymWorkoutsScreen(
                        onNavigateToWorkout = { navigate(Route.GymWorkout(it)) },
                        onCreateWorkoutClicked = { navigate(Route.CreateGymWorkout) }
                    )
                }
            }

            composable<Route.GymWorkout> {
                GymWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    releaseNavigationGuard = ::releaseNavigationGuard
                )
            }

            composable<Route.CreateGymWorkout> {
                CreateGymWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    releaseNavigationGuard = ::releaseNavigationGuard,
                )
            }

            navigation<Route.CardioMain>(startDestination = Route.CardioWorkouts) {
                composable<Route.CardioWorkouts> {
                    CardioWorkoutsScreen(
                        onNavigateToWorkout = { navigate(Route.CardioWorkout(it)) },
                        onNavigateToCreateCardio = { navigate(Route.CreateCardioWorkout) }
                    )
                }
            }

            composable<Route.CardioWorkout> {
                CardioWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    releaseNavigationGuard = ::releaseNavigationGuard
                )
            }
            composable<Route.CreateCardioWorkout> {
                CreateCardioWorkoutScreen(
                    onNavigateBack = ::popBackStack
                )
            }

            navigation<Route.StatsMain>(startDestination = Route.StatsOverview) {
                composable<Route.StatsOverview> {
                    StatsOverviewScreen(
                        onNavigateBack = ::popBackStack,
                        onSessionNavigate = { id ->
                            // TODO
                            /*if (type == WorkoutType.GYM) {
                                navigate(Route.GymSession(id))
                            } else {
                                navigate(Route.CardioSession(id))
                            }*/
                        },
                        onAddSessionNavigate = { _, type ->
                        },
                        onWorkoutStatsNavigate = {
                            // TODO
                            /*if (it.type == WorkoutType.GYM) {
                                navigate(Route.SplitStats(it.id))
                            } else {
                                navigate(Route.CardioStats(it.id))
                            }*/
                        }
                    )
                }
                composable<Route.GymWorkoutSession> {
                    GymWorkoutSessionStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.CardioWorkoutSession> {
                    CardioWorkoutSessionScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.GymWorkoutStats> {
                    GymWorkoutStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.CardioWorkoutStats> {
                    CardioWorkoutStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
            }

            composable<Route.Info> {
                InfoScreen(
                    onNavigateBack = ::popBackStack,
                    onDeleteFinished = { navigate(Route.Welcome) }
                )
            }
        }
    }

    if (unsavedChangesDialogOpen) {
        UnsavedChangesDialog(
            onConfirm = { doNotAskAgain ->
                if (doNotAskAgain) {
                    viewModel.stopAskingUnsavedChanges()
                }
                releaseNavigationGuard()
            },
            onCancel = { unsavedChangesDialogOpen = false }
        )
    }
}

@Composable
private fun keyboardAsState(): State<Boolean> {
    val ime = WindowInsets.ime
    val isOpen = ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isOpen)
}