package com.example.gymtracker.ui.gym.creategymworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.entity.gym.Exercise
import com.example.gymtracker.ui.gym.common.ExerciseListCreate
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.utility.WORKOUT_NAME_MAX_SIZE
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

    ProvideTopAppBar(
        title = {
            TopBarTextField(
                value = uiState.workoutName,
                onValueChange = viewModel::onWorkoutNameChange,
                maxSize = WORKOUT_NAME_MAX_SIZE
            )
        },
        navigationItem = {
            IconButton(
                onClick = { onNavigateBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = {
            releaseNavigationGuard()
            viewModel.onCreateWorkoutPressed { onNavigateBack() }
        },
        enabled = uiState.workoutName.isNotEmpty()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.save),
            contentDescription = stringResource(id = R.string.save)
        )
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