package com.example.gymtracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.WorkoutRepository
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
    val workoutDays: List<Instant> = emptyList(),
    val sessionDays: List<Instant> = emptyList(),
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
            val sessionTimestamps = sessions.mapNotNull { it?.timestamp }
            val zoneId = ZoneId.systemDefault()
            val workoutTimestamps = sessionTimestamps
                .groupBy { it.atZone(zoneId).toLocalDate() }
                .map { (_, dailyInstants) -> dailyInstants.first() }
            _uiState.update {
                it.copy(
                    sessionDays = sessionTimestamps,
                    workoutDays = workoutTimestamps,
                    loading = false
                )
            }
        }
    }
}