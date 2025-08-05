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
    class Split(val id: Int, val timestampString: String? = null) : Route()

    @Serializable
    object CreateSplit : Route()

    @Serializable
    object Cardio : Route()

    @Serializable
    object CardioList : Route()

    @Serializable
    class CardioItem(val id: Int, val timestampString: String? = null) : Route()

    @Serializable
    object CreateCardio : Route()

    @Serializable
    object Stats : Route()

    @Serializable
    object StatsOverview : Route()

    @Serializable
    class SplitStats(val id: Int) : Route()

    @Serializable
    class CardioStats(val id: Int) : Route()

    @Serializable
    class GymSession(val id: Int) : Route()

    @Serializable
    class CardioSession(val id: Int) : Route()

    @Serializable
    object Info : Route()
}

data class NavigationBarRoute(val titleResInt: Int, val route: Route, val iconResInt: Int)

val navigationBarRoutes = listOf(
    NavigationBarRoute(
        titleResInt = R.string.gym,
        route = Route.Gym,
        iconResInt = R.drawable.weight
    ),
    NavigationBarRoute(
        titleResInt = R.string.cardio,
        route = Route.Cardio,
        iconResInt = R.drawable.run
    ),
    NavigationBarRoute(
        titleResInt = R.string.stats,
        route = Route.Stats,
        iconResInt = R.drawable.stats
    ),
    NavigationBarRoute(
        titleResInt = R.string.info,
        route = Route.Info,
        iconResInt = R.drawable.info
    )
)