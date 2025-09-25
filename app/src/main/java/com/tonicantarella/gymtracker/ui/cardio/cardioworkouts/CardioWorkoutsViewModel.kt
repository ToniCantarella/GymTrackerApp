package com.tonicantarella.gymtracker.ui.cardio.cardioworkouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioWorkoutsUiState(
    val loading: Boolean = true,
    val workouts: List<WorkoutWithTimestamp> = emptyList(),
    val selectedItems: List<WorkoutWithTimestamp> = emptyList(),
    val selectingItems: Boolean = false
)

class CardioWorkoutsViewModel(
    private val workoutRepository: CardioWorkoutRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardioWorkoutsUiState())
    val uiState = _uiState.asStateFlow()

    fun getWorkouts() {
        viewModelScope.launch {
            val cardioList = workoutRepository.getAllWorkouts()
            _uiState.update {
                it.copy(
                    workouts = cardioList,
                    loading = false
                )
            }
        }
    }

    fun startSelectingItems(workout: WorkoutWithTimestamp? = null) {
        _uiState.update {
            it.copy(
                selectingItems = true,
                selectedItems = listOfNotNull(workout)
            )
        }
    }

    fun stopSelectingItems() {
        _uiState.update {
            it.copy(
                selectingItems = false,
                selectedItems = emptyList()
            )
        }
    }

    fun onSelectItem(workout: WorkoutWithTimestamp) {
        val isSelected = workout in uiState.value.selectedItems
        if (isSelected) {
            _uiState.update {
                it.copy(
                    selectedItems = it.selectedItems - workout
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedItems = it.selectedItems + workout
                )
            }
        }
    }

    fun onDeleteWorkouts() {
        val itemsToDelete = uiState.value.selectedItems

        viewModelScope.launch {
            itemsToDelete.forEach {
                workoutRepository.deleteWorkout(it.id)
            }
            stopSelectingItems()
            getWorkouts()
        }
    }

    fun onNavigateToWorkout(id: Int) {
        navigator.navigate(Route.CardioWorkout(id))
    }

    fun onCreateCardioClicked(){
        navigator.navigate(Route.CardioWorkout())
    }
}