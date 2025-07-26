package com.example.gymtracker.ui.navigation

import com.example.gymtracker.R
import kotlinx.serialization.Serializable

@Serializable
open class Route {
    @Serializable
    object Welcome : Route()

    @Serializable
    object Gym : Route()

    @Serializable
    object SplitList : Route()

    @Serializable
    class Split(val id: Int) : Route()

    @Serializable
    object CreateSplit : Route()

    @Serializable
    object Cardio : Route()

    @Serializable
    object CardioList : Route()

    @Serializable
    class CardioItem(val id: Int) : Route()

    @Serializable
    object CreateCardio : Route()

    @Serializable
    object Stats : Route()

    @Serializable
    object StatsOverview : Route()

    @Serializable
    object Info: Route()
}

data class BottomBarRoute(val titleResInt: Int, val route: Route, val iconResInt: Int)

val bottomBarRoutes = listOf(
    BottomBarRoute(
        titleResInt = R.string.gym,
        route = Route.Gym,
        iconResInt = R.drawable.weight
    ),
    BottomBarRoute(
        titleResInt = R.string.cardio,
        route = Route.Cardio,
        iconResInt = R.drawable.run
    ),
    BottomBarRoute(
        titleResInt = R.string.stats,
        route = Route.Stats,
        iconResInt = R.drawable.stats
    ),
    BottomBarRoute(
        titleResInt = R.string.info,
        route = Route.Info,
        iconResInt = R.drawable.info
    )
)