package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.repository.StatsRepository
import com.tonicantarella.gymtracker.repository.cardio.CardioSessionRepository
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import com.tonicantarella.gymtracker.repository.gym.GymSessionRepository
import com.tonicantarella.gymtracker.repository.gym.GymWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.statsoverview.CardioWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.Workout
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutLegend
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.time.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@OptIn(ExperimentalTime::class)
fun firstDayOfMonthInstant(
    zoneId: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    val today = Clock.System.now().toLocalDateTime(zoneId).date
    val firstOfMonth = LocalDate(today.year, today.month, 1)
    return firstOfMonth.atStartOfDayIn(zoneId).toJavaInstant()
}

// Todo maybe split this into multiple states? like data class CalendarState...
data class StatsOverviewUiState(
    val loading: Boolean = true,
    val gymWorkouts: List<WorkoutWithTimestamp> = emptyList(),
    val cardioWorkouts: List<WorkoutWithTimestamp> = emptyList(),
    val gymWorkoutsGeneralStats: List<GymWorkoutWithGeneralStats> = emptyList(),
    val cardioWorkoutsGeneralStats: List<CardioWorkoutWithGeneralStats> = emptyList(),
    val gymLegends: List<WorkoutLegend> = emptyList(),
    val cardioLegends: List<WorkoutLegend> = emptyList(),
    val calendarSessions: Map<LocalDate, List<WorkoutSession>> = emptyMap(),
    val calendarGymLegends: List<WorkoutLegend> = emptyList(),
    val calendarCardioLegends: List<WorkoutLegend> = emptyList(),
    val startDate: Instant = firstDayOfMonthInstant(),
    val endDate: Instant = Instant.now(),
    val gymColorIndexMap: Map<Int, Int> = emptyMap(),
    val cardioColorIndexMap: Map<Int, Int> = emptyMap()
)

class StatsOverviewViewModel(
    private val gymWorkoutRepository: GymWorkoutRepository,
    private val cardioWorkoutRepository: CardioWorkoutRepository,
    private val gymSessionRepository: GymSessionRepository,
    private val cardioSessionRepository: CardioSessionRepository,
    private val statsRepository: StatsRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsOverviewUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchAllStats() {
        viewModelScope.launch {
            fetchAllWorkouts()
            fetchAllWorkoutSessions()
            fetchWorkoutSessionsBetweenDates(
                startDate = uiState.value.startDate,
                endDate = uiState.value.endDate
            )
            fetchAllGeneralStats()
            _uiState.update {
                it.copy(
                    loading = false
                )
            }
        }
    }

    fun getMonthData(startDate: Instant, endDate: Instant) {
        viewModelScope.launch {
            fetchWorkoutSessionsBetweenDates(
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun onGymSessionNavigate(sessionId: Int) {
        navigator.navigate(Route.GymWorkoutSession(sessionId))
    }

    fun onCardioSessionNavigate(sessionId: Int) {
        navigator.navigate(Route.CardioWorkoutSession(sessionId))
    }

    fun onAddGymSessionNavigate(workoutId: Int, timestamp: Instant) {
        navigator.navigate(Route.GymWorkout(workoutId, timestamp.toString()))
    }

    fun onAddCardioSessionNavigate(workoutId: Int, timestamp: Instant) {
        navigator.navigate(Route.CardioWorkout(workoutId, timestamp.toString()))
    }

    fun onGymWorkoutStatsNavigate(workoutId: Int) {
        navigator.navigate(Route.GymWorkoutStats(workoutId))
    }

    fun onCardioWorkoutStatsNavigate(workoutId: Int) {
        navigator.navigate(Route.CardioWorkoutStats(workoutId))
    }

    private suspend fun fetchAllWorkouts() {
        val gymWorkouts = gymWorkoutRepository.getAllWorkouts()
        val cardioWorkouts = cardioWorkoutRepository.getAllWorkouts()

        val gymColorIndexMap = gymWorkouts
            .mapIndexed { index, workout -> workout.id to index }
            .toMap()
        val cardioColorIndexMap = cardioWorkouts
            .mapIndexed { index, workout -> workout.id to index }
            .toMap()
        _uiState.update {
            it.copy(
                gymWorkouts = gymWorkouts,
                cardioWorkouts = cardioWorkouts,
                gymColorIndexMap = gymColorIndexMap,
                cardioColorIndexMap = cardioColorIndexMap
            )
        }
    }

    private suspend fun fetchAllWorkoutSessions() {
        val gymSessions = gymSessionRepository.getAllSessions()
        val gymSessionsGrouped = gymSessions.groupBy { session -> session.workoutId }
        val gymLegends = uiState.value.gymWorkouts
            .map { workout ->
                WorkoutLegend(
                    sessionCount = gymSessionsGrouped[workout.id]?.size ?: 0,
                    workout = Workout(
                        id = workout.id,
                        name = workout.name,
                        type = WorkoutType.GYM
                    )
                )
            }
        val cardioSessions = cardioSessionRepository.getAllSessions()
        val cardioSessionsGrouped = cardioSessions.groupBy { session -> session.workoutId }
        val cardioLegends = uiState.value.cardioWorkouts
            .map { workout ->
                WorkoutLegend(
                    sessionCount = cardioSessionsGrouped[workout.id]?.size ?: 0,
                    workout = Workout(
                        id = workout.id,
                        name = workout.name,
                        type = WorkoutType.CARDIO
                    )
                )
            }
        _uiState.update {
            it.copy(
                gymLegends = gymLegends,
                cardioLegends = cardioLegends
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchWorkoutSessionsBetweenDates(startDate: Instant, endDate: Instant) {
        val gymSessionsForMonth = gymSessionRepository.getSessionsForTimespan(startDate, endDate)
        val cardioSessionsForMonth =
            cardioSessionRepository.getSessionsForTimespan(startDate, endDate)
        val sessionsForMonth = gymSessionsForMonth + cardioSessionsForMonth

        val gymSessionsGrouped =
            sessionsForMonth.filter { it.type == WorkoutType.GYM }.groupBy { it.workoutId }
        val cardioSessionsGrouped =
            sessionsForMonth.filter { it.type == WorkoutType.CARDIO }.groupBy { it.workoutId }

        val calendarGymLegends = uiState.value.gymWorkouts
            .map { workout ->
                WorkoutLegend(
                    sessionCount = gymSessionsGrouped[workout.id]?.size ?: 0,
                    workout = Workout(
                        id = workout.id,
                        name = workout.name,
                        type = WorkoutType.GYM
                    )
                )
            }

        val calendarCardioLegends = uiState.value.cardioWorkouts
            .map { workout ->
                WorkoutLegend(
                    sessionCount = cardioSessionsGrouped[workout.id]?.size ?: 0,
                    workout = Workout(
                        id = workout.id,
                        name = workout.name,
                        type = WorkoutType.CARDIO
                    )
                )
            }

        val calendarSessions = sessionsForMonth
            .groupBy { session ->
                session.timestamp
                    .toKotlinInstant()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            }

        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                calendarSessions = calendarSessions,
                calendarGymLegends = calendarGymLegends,
                calendarCardioLegends = calendarCardioLegends
            )
        }
    }

    private suspend fun fetchAllGeneralStats() {
        val gymWorkoutsStats = statsRepository.getAllGymWorkoutsWithGeneralStats()
        val cardioWorkoutsStats = statsRepository.getAllCardioWorkoutsWithGeneralStats()

        _uiState.update {
            it.copy(
                gymWorkoutsGeneralStats = gymWorkoutsStats,
                cardioWorkoutsGeneralStats = cardioWorkoutsStats
            )
        }
    }
}