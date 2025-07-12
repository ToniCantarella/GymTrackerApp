package com.example.gymtracker.ui.workouts.split

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.Exercise
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
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

    if (uiState.adding) {
        ProvideFloatingActionButton(
            onClick = viewModel::onCreateSplitPressed
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null
            )
        }
    } else {
        ProvideFloatingActionButton(
            onClick = viewModel::onFinishWorkoutPressed
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null
            )
        }
    }

    SplitScreen(
        loading = uiState.loading,
        exercises = uiState.exercises,
        addingSplit = uiState.adding,
        addExercise = viewModel::addExercise,
        onExerciseNameChange = viewModel::onExerciseNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        addSet = viewModel::addSet,
        onChangeWeight = viewModel::onChangeWeight,
        onChangeRepetitions = viewModel::onChangeRepetitions,
        onRemoveSet = viewModel::onRemoveSet,
        onCheckSet = viewModel::onCheckSet
    )
}

@Composable
fun SplitScreen(
    loading: Boolean,
    exercises: List<Exercise>,
    addingSplit: Boolean,
    addExercise: () -> Unit,
    onExerciseNameChange: (exerciseId: UUID, name: String) -> Unit,
    onDescriptionChange: (exerciseId: UUID, name: String) -> Unit,
    addSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, Int) -> Unit,
    onCheckSet: (set: WorkoutSet, checked: Boolean) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            LazyColumn (
                contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(exercises) { index, exercise ->
                    Exercise(
                        index = index + 1,
                        exercise = exercise,
                        onNameChange = { name -> onExerciseNameChange(exercise.uuid, name) },
                        onDescriptionChange = { description ->
                            onDescriptionChange(
                                exercise.uuid,
                                description
                            )
                        },
                        addSet = { addSet(exercise.uuid) },
                        onChangeWeight = { setId, weight ->
                            onChangeWeight(
                                exercise.uuid,
                                setId,
                                weight
                            )
                        },
                        onChangeRepetitions = { setId, repetitions ->
                            onChangeRepetitions(
                                exercise.uuid,
                                setId,
                                repetitions
                            )
                        },
                        onRemoveSet = { setId -> onRemoveSet(exercise.uuid, setId) },
                        onCheckSet = onCheckSet,
                        addingExercise = addingSplit
                    )
                }
                item{
                    Button(
                        onClick = addExercise,
                        enabled = exercises.last().name.isNotEmpty() && exercises.size < MAX_EXERCISES
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
}

@Composable
private fun ScreenForPreview(
    adding: Boolean = true
) {
    SplitScreen(
        loading = false,
        exercises = listOf(
            Exercise(
                name = "Bench press",
                uuid = UUID.randomUUID(),
                description = "Remember to warm up shoulders",
                sets = listOf(
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 100.0,
                        repetitions = 10
                    ),
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 80.0,
                        repetitions = 10
                    )
                )
            ),
            Exercise(
                name = if (!adding) "Overhead press" else "",
                uuid = UUID.randomUUID(),
                description = null,
                sets = if (!adding) listOf(
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 30.0,
                        repetitions = 10
                    ),
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 80.0,
                        repetitions = 10
                    )
                ) else listOf(
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 0.0,
                        repetitions = 0
                    ),
                )
            )
        ),
        addingSplit = adding,
        addExercise = {},
        onExerciseNameChange = { _, _ -> },
        onDescriptionChange = { _, _ -> },
        addSet = {},
        onChangeWeight = { _, _, _ -> },
        onChangeRepetitions = { _, _, _ -> },
        onCheckSet = { _, _ -> },
        onRemoveSet = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddSplitPreview() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}

@Preview(showBackground = true, locale = "fi", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddSplitPreviewFi() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitPreview() {
    GymTrackerTheme {
        ScreenForPreview(adding = false)
    }
}