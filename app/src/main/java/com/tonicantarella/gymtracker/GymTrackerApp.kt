package com.tonicantarella.gymtracker

import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.tonicantarella.gymtracker.ui.cardio.cardioworkouts.CardioWorkoutsScreen
import com.tonicantarella.gymtracker.ui.gym.gymworkout.GymWorkoutScreen
import com.tonicantarella.gymtracker.ui.gym.gymworkouts.GymWorkoutsScreen
import com.tonicantarella.gymtracker.ui.info.InfoScreen
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import com.tonicantarella.gymtracker.ui.stats.cardio.CardioSessionStatsScreen
import com.tonicantarella.gymtracker.ui.stats.cardio.CardioWorkoutStatsList
import com.tonicantarella.gymtracker.ui.stats.gym.GymSessionStatsScreen
import com.tonicantarella.gymtracker.ui.stats.gym.GymWorkoutStatsScreen
import com.tonicantarella.gymtracker.ui.stats.overview.StatsOverviewScreen
import com.tonicantarella.gymtracker.ui.welcome.WelcomeScreen

@Composable
fun GymTrackerApp(
    viewModel: MainViewModel,
    navigator: Navigator
) {
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }

    Surface(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        GymAppNavHost(
            viewModel = viewModel,
            navigator = navigator,
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
    modifier: Modifier = Modifier,
    navigator: Navigator
) {
    val mainUiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    LaunchedEffect(navigator) {
        Log.d("main", "main nav: ${navController}")
        navigator.registerNavController(navController)
    }

    val navigationAnimationMoveInt = 1500
    val enter =
        if (isLandscape || isWideScreen) fadeIn()
        else slideInHorizontally { navigationAnimationMoveInt }
    val exit =
        if (isLandscape || isWideScreen) fadeOut()
        else slideOutHorizontally { -navigationAnimationMoveInt }
    val popEnter =
        if (isLandscape || isWideScreen) fadeIn()
        else slideInHorizontally { -navigationAnimationMoveInt }
    val popExit =
        if (isLandscape || isWideScreen) fadeOut()
        else slideOutHorizontally { navigationAnimationMoveInt }

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

    val matchesNavigationItemRoute = navigationBarItems.any { navigationBarRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(navigationBarRoute.route::class) } == true
    }
    val layoutType = when {
        !matchesNavigationItemRoute -> NavigationSuiteType.None
        isLandscape -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
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
                    onClick = { navigator.navigate(item.route) },
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
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.primary.copy(alpha = .8f)
                )
            )
        )
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
                        navigator.navigate(Route.GymMain)
                    }
                )
            }

            navigation<Route.GymMain>(startDestination = Route.GymWorkouts) {
                composable<Route.GymWorkouts> {
                    GymWorkoutsScreen(
                        onNavigateToWorkout = { navigator.navigate(Route.GymWorkout(it)) },
                        onCreateWorkoutClicked = { navigator.navigate(Route.CreateGymWorkout) }
                    )
                }
            }

            composable<Route.GymWorkout> {
                GymWorkoutScreen()
            }

            composable<Route.CreateGymWorkout> {
                // TODO
                /*CreateGymWorkoutScreen(
                    onNavigateBack = navigationGuard::popBackStack,
                    navigationGuard = navigationGuard
                )*/
            }

            navigation<Route.CardioMain>(startDestination = Route.CardioWorkouts) {
                composable<Route.CardioWorkouts> {
                    CardioWorkoutsScreen(
                        onNavigateToWorkout = { navigator.navigate(Route.CardioWorkout(it)) },
                        onNavigateToCreateCardio = { navigator.navigate(Route.CreateCardioWorkout) }
                    )
                }
            }

            composable<Route.CardioWorkout> {
                // TODO
                /*CardioWorkoutScreen(
                    onNavigateBack = navigationGuard::popBackStack,
                    navigationGuard = navigationGuard
                )*/
            }
            composable<Route.CreateCardioWorkout> {
                //TODO
                /*CreateCardioWorkoutScreen(
                    onNavigateBack = navigationGuard::popBackStack,
                    navigationGuard = navigationGuard
                )*/
            }

            navigation<Route.StatsMain>(startDestination = Route.StatsOverview) {
                composable<Route.StatsOverview> {
                    StatsOverviewScreen(
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
                        onNavigateBack = navigator::popBackStack
                    )
                }
                composable<Route.CardioWorkoutSession> {
                    CardioSessionStatsScreen(
                        onNavigateBack = navigator::popBackStack
                    )
                }
                composable<Route.GymWorkoutStats> {
                    GymWorkoutStatsScreen(
                        onNavigateBack = navigator::popBackStack
                    )
                }
                composable<Route.CardioWorkoutStats> {
                    CardioWorkoutStatsList(
                        onNavigateBack = navigator::popBackStack
                    )
                }
            }

            composable<Route.Info> {
                InfoScreen(
                    onDeleteFinished = { navigator.navigate(Route.Welcome) }
                )
            }
        }
    }
}