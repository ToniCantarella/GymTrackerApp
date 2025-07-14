package com.example.gymtracker.ui.workouts.createsplit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.workouts.split.Exercise
import com.example.gymtracker.ui.workouts.split.ExerciseList
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun CreateSplitScreen(
    onNavigateBack: () -> Unit,
    onCreate: () -> Unit,
    viewModel: CreateSplitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            OutlinedTextField(
                value = uiState.splitName,
                onValueChange = viewModel::onSplitNameChange,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.name)
                    )
                },
                trailingIcon = {
                    if (uiState.splitName.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSplitNameChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = { viewModel.onCreateSplitPressed { onCreate() } },
        visible = uiState.exercises.last().name.isNotEmpty() && uiState.splitName.isNotEmpty()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.save),
            contentDescription = null
        )
    }

    CreateSplitScreen(
        exercises = uiState.exercises,
        addExercise = viewModel::addExercise,
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
            addingSplit = true,
            onAddExercise = addExercise,
            onExerciseNameChange = onExerciseNameChange,
            onDescriptionChange = onDescriptionChange,
            onAddSet = addSet,
            onChangeWeight = onChangeWeight,
            onChangeRepetitions = onChangeRepetitions,
            onCheckSet = { _, _, _ -> },
            onRemoveSet = onRemoveSet
        )
    }
}