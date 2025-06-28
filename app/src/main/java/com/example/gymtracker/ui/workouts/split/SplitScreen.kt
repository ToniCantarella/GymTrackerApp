package com.example.gymtracker.ui.workouts.split

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.Exercise
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun SplitScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitViewModel = koinViewModel()
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
        onClick = viewModel::onDonePressed
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null
        )
    }

    SplitScreen(
        exercises = uiState.exercises,
        addingSplit = uiState.adding,
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
fun SplitScreen(
    exercises: List<Exercise>,
    addingSplit: Boolean,
    addExercise: () -> Unit,
    onExerciseNameChange: (exerciseId: UUID, name: String) -> Unit,
    onDescriptionChange: (exerciseId: UUID, name: String) -> Unit,
    addSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, Int) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            itemsIndexed(exercises) { index, exercise ->
                Exercise (
                    index = index + 1,
                    exercise = exercise,
                    onNameChange = { name -> onExerciseNameChange(exercise.exerciseId, name) },
                    onDescriptionChange = { description ->
                        onDescriptionChange(
                            exercise.exerciseId,
                            description
                        )
                    },
                    addSet = { addSet(exercise.exerciseId) },
                    onChangeWeight = { setId, weight ->
                        onChangeWeight(
                            exercise.exerciseId,
                            setId,
                            weight
                        )
                    },
                    onChangeRepetitions = { setId, repetitions ->
                        onChangeRepetitions(
                            exercise.exerciseId,
                            setId,
                            repetitions
                        )
                    },
                    onRemoveSet = { setId -> onRemoveSet(exercise.exerciseId, setId) }
                )
            }
            item {
                Button(
                    onClick = addExercise
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.exercise)
                    )
                }
            }
        }
    }
}