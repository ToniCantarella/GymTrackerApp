package com.example.gymtracker.ui.gym.creategymworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.gymtracker.ui.entity.gym.Exercise
import com.example.gymtracker.ui.gym.common.ExerciseListCreate
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun CreateGymWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    releaseNavigationGuard: () -> Unit,
    viewModel: CreateGymWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges = uiState.workoutName.isNotEmpty() || uiState.exercises != uiState.initialExercises

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(hasUnsavedChanges) {
        if (hasUnsavedChanges) {
            onNavigationGuardChange(true)
        } else {
            onNavigationGuardChange(false)
        }
    }

    CreateGymWorkoutScreen(
        exercises = uiState.exercises,
        addExercise = viewModel::addExercise,
        onRemoveExercise = viewModel::onRemoveExercise,
        onExerciseNameChange = viewModel::onExerciseNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        addSet = viewModel::addSet,
        onChangeWeight = viewModel::onChangeWeight,
        onChangeRepetitions = viewModel::onChangeRepetitions,
        onRemoveSet = viewModel::onRemoveSet
    )
}

@Composable
private fun CreateGymWorkoutScreen(
    exercises: List<Exercise>,
    addExercise: () -> Unit,
    onRemoveExercise: (exerciseId: UUID) -> Unit,
    onExerciseNameChange: (exerciseId: UUID, name: String) -> Unit,
    onDescriptionChange: (exerciseId: UUID, name: String) -> Unit,
    addSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, Int) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit
) {
    Column {
        ExerciseListCreate(
            exercises = exercises,
            onAddExercise = addExercise,
            onRemoveExercise = onRemoveExercise,
            onExerciseNameChange = onExerciseNameChange,
            onDescriptionChange = onDescriptionChange,
            onAddSet = addSet,
            onChangeWeight = onChangeWeight,
            onChangeRepetitions = onChangeRepetitions,
            onRemoveSet = onRemoveSet
        )
    }
}