package com.example.gymtracker.ui.gym.gymworkoutplans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.GymRepository
import com.example.gymtracker.database.repository.WorkoutWithLatestTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GymWorkoutPlansUiState(
    val loading: Boolean = true,
    val workoutPlans: List<WorkoutWithLatestTimestamp> = emptyList(),
    val selectingItems: Boolean = false
)

class GymWorkoutPlansViewModel(
    private val gymRepository: GymRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GymWorkoutPlansUiState())
    val uiState = _uiState.asStateFlow()

    fun getWorkoutPlans() {
        viewModelScope.launch {
            val workoutPlans = gymRepository.getGymWorkoutPlans()
            _uiState.update {
                it.copy(
                    workoutPlans = workoutPlans,
                    loading = false
                )
            }
        }
    }

    fun startSelectingItems(id: Int? = null) {
        _uiState.update {
            it.copy(
                selectingItems = true,
                workoutPlans = it.workoutPlans.map { workout ->
                    if (workout.id == id) {
                        workout.copy(
                            selected = true
                        )
                    } else workout
                }
            )
        }
    }

    fun stopSelectingItems() {
        _uiState.update {
            it.copy(
                selectingItems = false,
                workoutPlans = it.workoutPlans.map { workout ->
                    workout.copy(
                        selected = false
                    )
                }
            )
        }
    }

    fun onSelectItem(id: Int, selected: Boolean) {
        _uiState.update {
            it.copy(
                workoutPlans = it.workoutPlans.map { workout ->
                    if (workout.id == id) {
                        workout.copy(
                            selected = selected
                        )
                    } else workout
                }
            )
        }
    }

    fun onDeleteWorkoutPlans(onDeletionDone: () -> Unit) {
        val itemsToDelete = uiState.value.workoutPlans.filter { it.selected }

        viewModelScope.launch {
            itemsToDelete.forEach {
                gymRepository.deleteGymWorkoutPlan(it.id)
            }
            stopSelectingItems()
            getWorkoutPlans()
            onDeletionDone()
        }
    }
}