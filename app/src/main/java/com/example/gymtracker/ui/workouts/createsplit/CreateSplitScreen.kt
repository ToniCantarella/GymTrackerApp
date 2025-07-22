package com.example.gymtracker.ui.workouts.createsplit

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.workouts.common.ExerciseList
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.utility.SPLIT_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun CreateSplitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateSplitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            TopBarTextField(
                value = uiState.splitName,
                onValueChange = viewModel::onSplitNameChange,
                maxSize = SPLIT_NAME_MAX_SIZE
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = { viewModel.onCreateSplitPressed { onNavigateBack() } },
        visible = uiState.exercises.last().name.isNotEmpty() && uiState.splitName.isNotEmpty()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.save),
            contentDescription = stringResource(id = R.string.save)
        )
    }

    CreateSplitScreen(
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
private fun CreateSplitScreen(
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
        ExerciseList(
            exercises = exercises,
            creatingSplit = true,
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