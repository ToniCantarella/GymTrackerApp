package com.tonicantarella.gymtracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
open class Route {
    @Serializable
    object Welcome : Route()

    @Serializable
    object GymMain : Route()

    @Serializable
    object GymWorkouts : Route()

    @Serializable
    class GymWorkout(val id: Int, val timestampString: String? = null) : Route()

    @Serializable
    object CreateGymWorkout : Route()

    @Serializable
    object CardioMain : Route()

    @Serializable
    object CardioWorkouts : Route()

    @Serializable
    class CardioWorkout(val id: Int, val timestampString: String? = null) : Route()

    @Serializable
    object CreateCardioWorkout : Route()

    @Serializable
    object StatsMain : Route()

    @Serializable
    object StatsOverview : Route()

    @Serializable
    class GymWorkoutStats(val id: Int) : Route()

    @Serializable
    class CardioWorkoutStats(val id: Int) : Route()

    @Serializable
    class GymWorkoutSession(val id: Int) : Route()

    @Serializable
    class CardioWorkoutSession(val id: Int) : Route()

    @Serializable
    object Info : Route()
}