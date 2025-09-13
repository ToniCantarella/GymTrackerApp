package com.tonicantarella.gymtracker.ui.gym.creategymworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.gym.common.ExerciseListCreate
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGymWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    releaseNavigationGuard: () -> Unit,
    viewModel: CreateGymWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges =
        uiState.workoutName.isNotEmpty() || uiState.exercises != uiState.initialExercises

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopBarTextField(
                        value = uiState.workoutName,
                        onValueChange = viewModel::onWorkoutNameChange
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val enabled = uiState.workoutName.isNotEmpty()
            FloatingActionButton(
                onClick = {
                    if (enabled){
                        releaseNavigationGuard()
                        viewModel.onCreateWorkoutPressed { onNavigateBack() }
                    }
                },
                containerColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = if (enabled) Color.White else Color.Black.copy(alpha = 0.5f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        }
    ) { innerPadding ->
        CreateGymWorkoutScreen(
            exercises = uiState.exercises,
            addExercise = viewModel::addExercise,
            onRemoveExercise = viewModel::onRemoveExercise,
            onExerciseNameChange = viewModel::onExerciseNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            addSet = viewModel::addSet,
            onChangeWeight = viewModel::onChangeWeight,
            onChangeRepetitions = viewModel::onChangeRepetitions,
            onRemoveSet = viewModel::onRemoveSet,
            modifier = Modifier.padding(innerPadding)
        )
    }
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
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
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