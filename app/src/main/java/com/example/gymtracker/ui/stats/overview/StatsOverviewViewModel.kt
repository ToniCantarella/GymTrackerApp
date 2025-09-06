package com.example.gymtracker.ui.stats.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.repository.StatsOverviewRepository
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.entity.statsoverview.CalendarLegend
import com.example.gymtracker.ui.entity.statsoverview.Workout
import com.example.gymtracker.ui.entity.statsoverview.WorkoutSession
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

data class StatsOverviewUiState(
    val loading: Boolean = true,
    val gymWorkouts: List<WorkoutWithTimestamp> = emptyList(),
    val cardioWorkouts: List<WorkoutWithTimestamp> = emptyList(),
    val gymSessions: List<WorkoutSession> = emptyList(),
    val cardioSessions: List<WorkoutSession> = emptyList(),
    val calendarWorkouts: Map<LocalDate, List<WorkoutSession>> = emptyMap(),
    val calendarLegends: List<CalendarLegend> = emptyList(),
    val startDate: Instant = firstDayOfMonthInstant(),
    val endDate: Instant = Instant.now(),
    val colorIndexMap: Map<Int, Int> = emptyMap()
)

class StatsOverviewViewModel(
    private val statsOverviewRepository: StatsOverviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsOverviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fetchAllWorkouts()
            fetchAllWorkoutSessions()
            fetchWorkoutSessionsBetweenDates(
                startDate = uiState.value.startDate,
                endDate = uiState.value.endDate
            )
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

    private suspend fun fetchAllWorkouts() {
        val gymWorkouts = statsOverviewRepository.getAllGymWorkouts()
        val cardioWorkouts = statsOverviewRepository.getAllCardioWorkouts()

        val gymColorIndexMap = gymWorkouts.mapIndexed { index, workout ->
            workout.id to index
        }.toMap()
        val cardioColorIndexMap = cardioWorkouts.mapIndexed { index, workout ->
            workout.id to index
        }.toMap()

        _uiState.update {
            it.copy(
                gymWorkouts = gymWorkouts,
                cardioWorkouts = cardioWorkouts,
                colorIndexMap = gymColorIndexMap + cardioColorIndexMap
            )
        }

    }

    private suspend fun fetchAllWorkoutSessions() {
        val gymSessions = statsOverviewRepository.getAllGymSessions()
        val cardioSessions = statsOverviewRepository.getAllCardioSessions()
        _uiState.update {
            it.copy(
                gymSessions = gymSessions,
                cardioSessions = cardioSessions
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchWorkoutSessionsBetweenDates(startDate: Instant, endDate: Instant) {
        val sessionsForMonth =
            statsOverviewRepository.getWorkoutSessionsForTimespan(startDate, endDate)

        val calendarLegends = sessionsForMonth
            .groupBy { session -> session.sessionId }
            .map { (_, sessions) ->
                CalendarLegend(
                    sessionCount = sessions.size,
                    workout = Workout(
                        name = sessions.first().workoutName,
                        type = sessions.first().type
                    )
                )
            }

        val calendarWorkouts = sessionsForMonth
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
                calendarWorkouts = calendarWorkouts,
                calendarLegends = calendarLegends
            )
        }
    }
}