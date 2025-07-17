package com.example.gymtracker.ui.stats

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
    val workouts: List<Workout> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList(),
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
            val sessions = workoutRepository.getSplitSessionsBetweenDates(
                startDate = uiState.value.startDate,
                endDate = uiState.value.endDate
            )
            _uiState.update {
                it.copy(
                    workoutSessions = sessions,
                    loading = false
                )
            }
        }
    }
}