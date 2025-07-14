package com.example.gymtracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val matchesBottomBarRoute = bottomBarRoutes.any { bottomBarRoute ->
        currentDestination?.hierarchy?.any { it.hasRoute(bottomBarRoute.route::class) } == true
    }

    Scaffold(
        topBar = {
            GymTopAppBar(
                navBackStackEntry = navBackStackEntry
            )
        },
        bottomBar = {
            if (matchesBottomBarRoute && currentDestination != null) {
                BottomAppBar {
                    bottomBarRoutes.forEach { bottomBarRoute ->
                        val selected = currentDestination.hierarchy.any {
                            it.hasRoute(bottomBarRoute.route::class)
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(bottomBarRoute.route) {
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(bottomBarRoute.iconResInt),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = bottomBarRoute.titleResId)
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