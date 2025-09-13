package com.tonicantarella.gymtracker

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.tonicantarella.gymtracker.ui.cardio.cardioworkout.CardioWorkoutScreen
import com.tonicantarella.gymtracker.ui.cardio.cardioworkouts.CardioWorkoutsScreen
import com.tonicantarella.gymtracker.ui.cardio.createcardioworkout.CreateCardioWorkoutScreen
import com.tonicantarella.gymtracker.ui.common.UnsavedChangesDialog
import com.tonicantarella.gymtracker.ui.gym.creategymworkout.CreateGymWorkoutScreen
import com.tonicantarella.gymtracker.ui.gym.gymworkout.GymWorkoutScreen
import com.tonicantarella.gymtracker.ui.gym.gymworkouts.GymWorkoutsScreen
import com.tonicantarella.gymtracker.ui.info.InfoScreen
import com.tonicantarella.gymtracker.ui.navigation.Route
import com.tonicantarella.gymtracker.ui.navigation.rememberNavigationGuard
import com.tonicantarella.gymtracker.ui.stats.cardio.CardioSessionStatsScreen
import com.tonicantarella.gymtracker.ui.stats.cardio.CardioWorkoutStatsScreen
import com.tonicantarella.gymtracker.ui.stats.gym.GymSessionStatsScreen
import com.tonicantarella.gymtracker.ui.stats.gym.GymWorkoutStatsScreen
import com.tonicantarella.gymtracker.ui.stats.overview.StatsOverviewScreen
import com.tonicantarella.gymtracker.ui.welcome.WelcomeScreen

@Composable
fun GymTrackerApp(
    viewModel: MainViewModel
) {
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }

    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        GymAppNavHost(
            viewModel = viewModel,
            modifier = Modifier
        )
    }
}

@Composable
private fun keyboardAsState(): State<Boolean> {
    val ime = WindowInsets.ime
    val isOpen = ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isOpen)
}

data class NavigationBarItem(val titleResInt: Int, val route: Route, val iconResInt: Int)

@Composable
fun GymAppNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val mainUiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val navigationAnimationMoveInt = 1500
    val enter =
        if (isLandscape|| isWideScreen)
            fadeIn()
        else
            slideInHorizontally { navigationAnimationMoveInt }
    val exit =
        if (isLandscape|| isWideScreen)
            fadeOut()
        else
            slideOutHorizontally { -navigationAnimationMoveInt }
    val popEnter =
        if (isLandscape || isWideScreen)
            fadeIn()
        else
            slideInHorizontally { -navigationAnimationMoveInt }
    val popExit =
        if (isLandscape|| isWideScreen)
            fadeOut()
        else
            slideOutHorizontally { navigationAnimationMoveInt }

    val navigationGuard = rememberNavigationGuard()

    fun navigate(route: Route) {
        navigationGuard.navigate {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    fun popBackStack() {
        navigationGuard.navigate {
            navController.popBackStack()
        }
    }

    val navigationBarItems = listOf(
        NavigationBarItem(
            titleResInt = R.string.gym,
            route = Route.GymMain,
            iconResInt = R.drawable.weight
        ),
        NavigationBarItem(
            titleResInt = R.string.cardio,
            route = Route.CardioMain,
            iconResInt = R.drawable.run
        ),
        NavigationBarItem(
            titleResInt = R.string.stats,
            route = Route.StatsMain,
            iconResInt = R.drawable.stats
        ),
        NavigationBarItem(
            titleResInt = R.string.info,
            route = Route.Info,
            iconResInt = R.drawable.info
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val matchesNavigationItemRoute = navigationBarItems.any { navigationBarRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(navigationBarRoute.route::class) } == true
    }

    val defaultLayoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    val layoutType = if (matchesNavigationItemRoute) {
        if (isLandscape) {
            NavigationSuiteType.NavigationRail
        } else {
            defaultLayoutType
        }
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            navigationBarItems.map { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(item.route::class)
                } ?: false

                item(
                    selected = selected,
                    onClick = { navigate(item.route) },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.iconResInt),
                            contentDescription = stringResource(id = item.titleResInt)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = item.titleResInt)
                        )
                    }
                )
            }
        }
    ) {
        NavHost(
            enterTransition = { enter },
            exitTransition = { exit },
            popEnterTransition = { popEnter },
            popExitTransition = { popExit },
            navController = navController,
            startDestination = mainUiState.initialRoute,
            modifier = modifier
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
                    onNavigationGuardChange = navigationGuard::guard,
                    releaseNavigationGuard = navigationGuard::release
                )
            }

            composable<Route.CreateGymWorkout> {
                CreateGymWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = navigationGuard::guard,
                    releaseNavigationGuard = navigationGuard::release
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
                    onNavigationGuardChange = navigationGuard::guard,
                    releaseNavigationGuard = navigationGuard::release
                )
            }
            composable<Route.CreateCardioWorkout> {
                CreateCardioWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = navigationGuard::guard,
                    releaseNavigationGuard = navigationGuard::release
                )
            }

            navigation<Route.StatsMain>(startDestination = Route.StatsOverview) {
                composable<Route.StatsOverview> {
                    StatsOverviewScreen(
                        onNavigateBack = ::popBackStack,
                        onGymSessionNavigate = { id ->
                            navController.navigate(Route.GymWorkoutSession(id))
                        },
                        onCardioSessionNavigate = { id ->
                            navController.navigate(Route.CardioWorkoutSession(id))
                        },
                        onAddGymSessionNavigate = { workoutId, timestamp ->
                            navController.navigate(
                                Route.GymWorkout(
                                    workoutId,
                                    timestamp.toString()
                                )
                            )
                        },
                        onAddCardioSessionNavigate = { workoutId, timestamp ->
                            navController.navigate(
                                Route.CardioWorkout(
                                    workoutId,
                                    timestamp.toString()
                                )
                            )
                        },
                        onGymWorkoutStatsNavigate = { id ->
                            navController.navigate(Route.GymWorkoutStats(id))
                        },
                        onCardioWorkoutStatsNavigate = { id ->
                            navController.navigate(Route.CardioWorkoutStats(id))
                        }
                    )
                }
                composable<Route.GymWorkoutSession> {
                    GymSessionStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.CardioWorkoutSession> {
                    CardioSessionStatsScreen(
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
                    onDeleteFinished = { navigate(Route.Welcome) }
                )
            }
        }

        if (navigationGuard.unsavedChangesDialogOpen) {
            UnsavedChangesDialog(
                onConfirm = { doNotAskAgain ->
                    if (doNotAskAgain) {
                        viewModel.stopAskingUnsavedChanges()
                    }
                    navigationGuard.release()
                },
                onCancel = navigationGuard::dismissDialog
            )
        }
    }
}