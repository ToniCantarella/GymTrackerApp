package com.example.gymtracker.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun GymScaffold(
    navController: NavController,
    navigate: (Route) -> Unit,
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val matchesNavigationBarRoute = navigationBarRoutes.any { navigationBarRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(navigationBarRoute.route::class) } == true
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isLandscape) {
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                } else {
                    Modifier
                }
            )

    ) {
        if (matchesNavigationBarRoute && currentDestination != null && isLandscape) {
            NavigationRail {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navigationBarRoutes.forEach { navigationBarRoute ->
                        val selected = currentDestination.hierarchy.any {
                            it.hasRoute(navigationBarRoute.route::class)
                        }
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navigate(navigationBarRoute.route)
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(navigationBarRoute.iconResInt),
                                    contentDescription = stringResource(id = navigationBarRoute.titleResInt),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = navigationBarRoute.titleResInt)
                                )
                            }
                        )
                    }
                }
            }
        }
        Scaffold(
            topBar = {
                GymTopAppBar(
                    navBackStackEntry = navBackStackEntry
                )
            },
            bottomBar = {
                if (matchesNavigationBarRoute && currentDestination != null && !isLandscape) {
                    BottomAppBar {
                        navigationBarRoutes.forEach { navigationBarRoute ->
                            val selected = currentDestination.hierarchy.any {
                                it.hasRoute(navigationBarRoute.route::class)
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navigate(navigationBarRoute.route)
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(navigationBarRoute.iconResInt),
                                        contentDescription = stringResource(id = navigationBarRoute.titleResInt),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = navigationBarRoute.titleResInt)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                GymFloatingActionButton(
                    navBackStackEntry = navBackStackEntry
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}