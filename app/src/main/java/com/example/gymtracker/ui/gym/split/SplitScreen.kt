package com.example.gymtracker.ui.gym.split

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.gym.common.ExerciseList
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.gym.entity.WorkoutSet
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideNavigationBarGuard
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.navigation.rememberProceedOnGuardCleared
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.SPLIT_NAME_MAX_SIZE
import com.example.gymtracker.utility.toDateAndTimeString
import com.example.gymtracker.utility.toDateString
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.util.UUID

@Composable
fun SplitScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStats: (id: Int) -> Unit,
    viewModel: SplitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var navigationDialogOpen by remember { mutableStateOf(false) }
    var navigationDialogOnNavigate: () -> Unit by remember { mutableStateOf({}) }

    val hasUnsavedChanges =
        uiState.initialSplitName != uiState.splitName || uiState.initialExercises != uiState.exercises

    fun navigationCheck(onNavigate: () -> Unit) {
        if (hasUnsavedChanges) {
            navigationDialogOpen = true
            navigationDialogOnNavigate = onNavigate
        } else {
            onNavigate()
        }

    }

    var finishWorkoutDialogOpen by remember { mutableStateOf(false) }

    fun onFinishWorkout() = viewModel.onFinishWorkoutPressed { onNavigateBack() }

    fun finishWorkoutCheck() {
        val hasSetsChecked = uiState.exercises.any { it.sets.any { set -> set.checked } }
        if (hasSetsChecked && uiState.showConfirmOnFinishWorkout) {
            finishWorkoutDialogOpen = true
        } else {
            onFinishWorkout()
        }
    }

    BackHandler {
        navigationCheck { onNavigateBack() }
    }

    ProvideNavigationBarGuard(
        isGuarded = hasUnsavedChanges,
        onGuard = { navigationDialogOpen = true }
    )
    val proceedOnGuardCleared = rememberProceedOnGuardCleared()

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
                onClick = { navigationCheck { onNavigateBack() } }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = { navigationCheck { onNavigateToStats(uiState.splitId) } }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.timeline),
                    contentDescription = stringResource(R.string.stats)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = ::finishWorkoutCheck
    ) {
        Icon(
            painter = painterResource(id = R.drawable.goal),
            contentDescription = stringResource(id = R.string.done)
        )
    }

    SplitScreen(
        loading = uiState.loading,
        latestTimestamp = uiState.latestTimestamp,
        addingTimestamp = uiState.selectedTimestamp,
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

    if (navigationDialogOpen) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(id = R.string.unsaved_changes),
                    textAlign = TextAlign.Center
                )
            },
            cancelButton = {
                OutlinedButton(
                    onClick = { navigationDialogOpen = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        navigationDialogOpen = false
                        navigationDialogOnNavigate.invoke()
                        proceedOnGuardCleared()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            onDismissRequest = { navigationDialogOpen = false }
        )
    }

    if (finishWorkoutDialogOpen) {
        ConfirmDialog(
            subtitle = {
                Column {
                    Text(
                        text = stringResource(id = R.string.confirm_done_workout),
                        textAlign = TextAlign.Center
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            uiState.doNotAskAgain,
                            onCheckedChange = viewModel::onShowFinishDialogChecked
                        )
                        Text(
                            text = stringResource(id = R.string.do_not_ask_again)
                        )
                    }
                }
            },
            cancelButton = {
                OutlinedButton(
                    onClick = { finishWorkoutDialogOpen = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        finishWorkoutDialogOpen = false
                        onFinishWorkout()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            onDismissRequest = { finishWorkoutDialogOpen = false }
        )
    }
}

@Composable
fun SplitScreen(
    loading: Boolean,
    latestTimestamp: Instant?,
    addingTimestamp: Instant?,
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
                if(addingTimestamp != null) {
                    Text(
                        text = "${stringResource(id = R.string.adding_for_date)}: ${addingTimestamp.toDateString()}"
                    )
                }    else
                {
                    Text(
                        text = "${stringResource(id = R.string.last_time)}: ${latestTimestamp?.toDateAndTimeString() ?: "-"}"
                    )
                }
            }
            ExerciseList(
                exercises = exercises,
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
        addingTimestamp = null,
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