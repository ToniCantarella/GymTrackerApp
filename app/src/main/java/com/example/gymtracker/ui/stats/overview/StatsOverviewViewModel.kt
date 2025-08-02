package com.example.gymtracker.ui.stats.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.Workout
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.database.repository.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun firstDayOfMonthInstant(zoneId: ZoneId = ZoneId.systemDefault()): Instant {
    return LocalDate.now(zoneId)
        .withDayOfMonth(1)
        .atStartOfDay(zoneId)
        .toInstant()
}

data class StatsOverviewUiState(
    val loading: Boolean = true,
    val gymWorkouts: List<Workout> = emptyList(),
    val cardioWorkouts: List<Workout> = emptyList(),
    val gymSessions: List<WorkoutSession> = emptyList(),
    val cardioSessions: List<WorkoutSession> = emptyList(),
    val workoutSessionsBetween: List<WorkoutSession> = emptyList(),
    val startDate: Instant = firstDayOfMonthInstant(),
    val endDate: Instant = Instant.now()
)

class StatsOverviewViewModel(
    private val workoutRepository: WorkoutRepository
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
        val gymWorkouts = workoutRepository.getGymWorkouts()
        val cardioWorkouts = workoutRepository.getCardioWorkouts()
        _uiState.update {
            it.copy(
                gymWorkouts = gymWorkouts,
                cardioWorkouts = cardioWorkouts
            )
        }
    }

    private suspend fun fetchAllWorkoutSessions() {
        val gymSessions = workoutRepository.getGymSessions()
        val cardioSessions = workoutRepository.getCardioSessions()
        _uiState.update {
            it.copy(
                gymSessions = gymSessions,
                cardioSessions = cardioSessions
            )
        }
    }

    private suspend fun fetchWorkoutSessionsBetweenDates(startDate: Instant, endDate: Instant) {
        val sessions = workoutRepository.getWorkoutSessionsBetween(
            start = startDate,
            end = endDate
        )
        _uiState.update {
            it.copy(
                workoutSessionsBetween = sessions
            )
        }
    }
}