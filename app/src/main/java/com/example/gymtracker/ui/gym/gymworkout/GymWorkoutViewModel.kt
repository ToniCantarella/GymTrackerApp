package com.example.gymtracker.ui.gym.gymworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymtracker.repository.gym.GymSessionRepository
import com.example.gymtracker.repository.gym.GymStatsRepository
import com.example.gymtracker.repository.gym.GymWorkoutRepository
import com.example.gymtracker.ui.entity.gym.Exercise
import com.example.gymtracker.ui.entity.gym.GymWorkoutStats
import com.example.gymtracker.ui.info.SHOW_FINISH_WORKOUT_DIALOG
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.utility.GymWorkoutUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class GymWorkoutUiState(
    val loading: Boolean = false,
    val workoutId: Int = 0,
    val workoutName: String = "",
    val latestTimestamp: Instant? = null,
    val exercises: List<Exercise> = emptyList(),
    val initialWorkoutName: String = "",
    val initialExercises: List<Exercise> = emptyList(),
    val guardFinishWorkout: Boolean = true,
    val doNotAskAgain: Boolean = false,
    val selectedTimestamp: Instant? = null,
    val stats: GymWorkoutStats? = null
)

class GymWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: GymWorkoutRepository,
    private val statsRepository: GymStatsRepository,
    private val sessionRepository: GymSessionRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val gymWorkoutUtil = GymWorkoutUtil()
    private val navParams = savedStateHandle.toRoute<Route.GymWorkout>()

    private val _uiState = MutableStateFlow(GymWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val latestWorkout = workoutRepository.getLatestWorkoutWithExercises(navParams.id)
            val workoutName = latestWorkout?.name ?: ""
            val exercises = latestWorkout?.exercises ?: emptyList()
            val selectedTimestamp = navParams.timestampString?.let{ Instant.parse(it) }

            val showFinishWorkoutDialog = dataStore.data
                .map { it[SHOW_FINISH_WORKOUT_DIALOG] ?: true }
                .first()

            _uiState.update {
                it.copy(
                    workoutId = navParams.id,
                    workoutName = workoutName,
                    latestTimestamp = latestWorkout?.timestamp,
                    selectedTimestamp = selectedTimestamp,
                    exercises = exercises,
                    initialWorkoutName = workoutName,
                    initialExercises = exercises,
                    guardFinishWorkout = showFinishWorkoutDialog,
                    loading = false
                )
            }

            val workoutStats = statsRepository.getWorkoutStats(navParams.id)

            _uiState.update {
                it.copy(
                    stats = workoutStats
                )
            }
        }
    }

    fun onWorkoutNameChange(name: String) {
        _uiState.update {
            it.copy(
                workoutName = name
            )
        }
    }

    fun onExerciseNameChange(id: UUID, name: String) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateExerciseName(it.exercises, id, name)
            )
        }
    }

    fun onDescriptionChange(id: UUID, description: String) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateExerciseDescription(it.exercises, id, description)
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
                exercises = gymWorkoutUtil.checkSet(it.exercises, exerciseId, setId, checked)
            )
        }
    }

    fun addSet(exerciseId: UUID) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.addSet(it.exercises, exerciseId)
            )
        }
    }

    fun onRemoveSet(exerciseId: UUID, setId: UUID) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.removeSet(it.exercises, exerciseId, setId)
            )
        }
    }

    fun onChangeWeight(exerciseId: UUID, setId: UUID, weight: Double) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateWeight(it.exercises, exerciseId, setId, weight)
            )
        }
    }

    fun onChangeRepetitions(exerciseId: UUID, setId: UUID, repetitions: Int) {
        _uiState.update {
            it.copy(
                exercises = gymWorkoutUtil.updateRepetitions(
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

    fun saveChanges() {
        viewModelScope.launch {
            workoutRepository.updateWorkout(
                workoutId = navParams.id,
                workoutName = uiState.value.workoutName,
                exercises = uiState.value.exercises
            )
        }
    }

    fun onFinishWorkoutPressed(onFinish: () -> Unit) {
        saveChanges()
        viewModelScope.launch {
            sessionRepository.markSessionDone(
                workoutId = navParams.id,
                exercises = uiState.value.exercises,
            )

            if (uiState.value.guardFinishWorkout && uiState.value.doNotAskAgain) {
                dataStore.edit { preferences ->
                    preferences[SHOW_FINISH_WORKOUT_DIALOG] = false
                }
            }

            onFinish()
        }
    }
}
