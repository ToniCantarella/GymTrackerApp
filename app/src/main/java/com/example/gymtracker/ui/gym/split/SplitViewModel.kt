package com.example.gymtracker.ui.gym.split

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.repository.GymRepository
import com.example.gymtracker.repository.SplitStats
import com.example.gymtracker.repository.StatRepository
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.info.SHOW_FINISH_WORKOUT_DIALOG
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.utility.SplitUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class SplitUiState(
    val loading: Boolean = false,
    val splitId: Int = 0,
    val splitName: String = "",
    val latestTimestamp: Instant? = null,
    val exercises: List<Exercise> = emptyList(),
    val initialSplitName: String = "",
    val initialExercises: List<Exercise> = emptyList(),
    val showConfirmOnFinishWorkout: Boolean = true,
    val doNotAskAgain: Boolean = false,
    val selectedTimestamp: Instant? = null,
    val stats: SplitStats? = null
)

class SplitViewModel(
    savedStateHandle: SavedStateHandle,
    private val gymRepository: GymRepository,
    private val statsRepository: StatRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val splitUtil = SplitUtil()
    private val navParams = savedStateHandle.toRoute<Route.Split>()

    private val _uiState = MutableStateFlow(SplitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val latestSplit = gymRepository.getLatestSplitWithExercises(navParams.id)
            val splitName = latestSplit?.name ?: ""
            val exercises = latestSplit?.exercises ?: emptyList()
            val selectedTimestamp = navParams.timestampString?.let{ Instant.parse(it) }

            val showFinishWorkoutDialog = dataStore.data
                .map { it[SHOW_FINISH_WORKOUT_DIALOG] ?: true }
                .first()

            _uiState.update {
                it.copy(
                    splitId = navParams.id,
                    splitName = splitName,
                    latestTimestamp = latestSplit?.timestamp,
                    selectedTimestamp = selectedTimestamp,
                    exercises = exercises,
                    initialSplitName = splitName,
                    initialExercises = exercises,
                    showConfirmOnFinishWorkout = showFinishWorkoutDialog,
                    loading = false
                )
            }

            val splitStats = statsRepository.getSplitStats(navParams.id)

            _uiState.update {
                it.copy(
                    stats = splitStats
                )
            }
        }
    }

    fun onSplitNameChange(name: String) {
        _uiState.update {
            it.copy(
                splitName = name
            )
        }
    }

    fun onExerciseNameChange(id: UUID, name: String) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateExerciseName(it.exercises, id, name)
            )
        }
    }

    fun onDescriptionChange(id: UUID, description: String) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateExerciseDescription(it.exercises, id, description)
            )
        }
    }

    fun addExercise() {
        _uiState.update {
            it.copy(
                exercises = it.exercises + listOf(Exercise.emptyExercise())
            )
        }
    }

    fun onRemoveExercise(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = it.exercises.filter { exercise -> exercise.uuid != exerciseId }
            )
        }
    }

    fun onCheckSet(exerciseId: UUID, setId: UUID, checked: Boolean) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.checkSet(it.exercises, exerciseId, setId, checked)
            )
        }
    }

    fun addSet(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.addSet(it.exercises, exerciseId)
            )
        }
    }

    fun onRemoveSet(exerciseId: UUID, setId: UUID) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.removeSet(it.exercises, exerciseId, setId)
            )
        }
    }

    fun onChangeWeight(exerciseId: UUID, setId: UUID, weight: Double) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateWeight(it.exercises, exerciseId, setId, weight)
            )
        }
    }

    fun onChangeRepetitions(exerciseId: UUID, setId: UUID, repetitions: Int) {
        _uiState.update {
            it.copy(
                exercises = splitUtil.updateRepetitions(
                    it.exercises,
                    exerciseId,
                    setId,
                    repetitions
                )
            )
        }
    }

    fun onShowFinishDialogChecked(checked: Boolean) {
        _uiState.update {
            it.copy(
                doNotAskAgain = checked
            )
        }
    }

    fun onFinishWorkoutPressed(onFinish: () -> Unit) {
        viewModelScope.launch {
            gymRepository.markSplitSessionDone(
                splitId = navParams.id,
                splitName = uiState.value.splitName,
                exercises = uiState.value.exercises,
                timestamp = uiState.value.selectedTimestamp
            )

            if (uiState.value.showConfirmOnFinishWorkout && uiState.value.doNotAskAgain) {
                dataStore.edit { preferences ->
                    preferences[SHOW_FINISH_WORKOUT_DIALOG] = false
                }
            }

            onFinish()
        }
    }
}
