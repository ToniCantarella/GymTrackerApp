package com.example.gymtracker.ui.workouts.split

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.ui.workouts.common.ExerciseList
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import com.example.gymtracker.utility.SPLIT_NAME_MAX_SIZE
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.util.UUID

@Composable
fun SplitScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitViewModel = koinViewModel()
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
        onClick = { viewModel.onFinishWorkoutPressed(onNavigateBack) }
    ) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = stringResource(id = R.string.done)
        )
    }

    SplitScreen(
        loading = uiState.loading,
        latestTimestamp = uiState.latestTimestamp,
        exercises = uiState.exercises,
        addExercise = viewModel::addExercise,
        onRemoveExercise = viewModel::onRemoveExercise,
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
    latestTimestamp: Instant?,
    exercises: List<Exercise>,
    addExercise: () -> Unit,
    onRemoveExercise: (exerciseId: UUID) -> Unit,
    onExerciseNameChange: (exerciseId: UUID, name: String) -> Unit,
    onDescriptionChange: (exerciseId: UUID, name: String) -> Unit,
    addSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, weight: Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, repetitions: Int) -> Unit,
    onCheckSet: (exerciseId: UUID, setId: UUID, checked: Boolean) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (exercises.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
            ) {
                Text(
                    text = "${stringResource(id = R.string.last_time)}: ${latestTimestamp?.toDateAndTimeString() ?: "-"}"
                )
            }
            ExerciseList(
                exercises = exercises,
                creatingSplit = false,
                onAddExercise = addExercise,
                onRemoveExercise = onRemoveExercise,
                onExerciseNameChange = onExerciseNameChange,
                onDescriptionChange = onDescriptionChange,
                onAddSet = addSet,
                onChangeWeight = onChangeWeight,
                onChangeRepetitions = onChangeRepetitions,
                onCheckSet = onCheckSet,
                onRemoveSet = onRemoveSet
            )
        }
    }
}

@Composable
private fun ScreenForPreview(
) {
    SplitScreen(
        loading = false,
        latestTimestamp = Instant.now(),
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
                name = "Overhead press",
                uuid = UUID.randomUUID(),
                description = null,
                sets = listOf(
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
                )
            )
        ),
        addExercise = {},
        onRemoveExercise = {},
        onExerciseNameChange = { _, _ -> },
        onDescriptionChange = { _, _ -> },
        addSet = {},
        onChangeWeight = { _, _, _ -> },
        onChangeRepetitions = { _, _, _ -> },
        onCheckSet = { _, _, _ -> },
        onRemoveSet = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun SplitPreview() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}