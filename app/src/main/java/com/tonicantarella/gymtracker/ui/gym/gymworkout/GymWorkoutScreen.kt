package com.tonicantarella.gymtracker.ui.gym.gymworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.ConfirmDialog
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutSet
import com.tonicantarella.gymtracker.ui.gym.common.ExerciseListEdit
import com.tonicantarella.gymtracker.ui.stats.BasicLineChart
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.WORKOUT_NAME_MAX_SIZE
import com.tonicantarella.gymtracker.utility.toDateAndTimeString
import com.tonicantarella.gymtracker.utility.toDateString
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    releaseNavigationGuard: () -> Unit,
    viewModel: GymWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var statsBottomSheetOpen by remember { mutableStateOf(false) }
    var finishWorkoutDialogOpen by remember { mutableStateOf(false) }

    val hasUnsavedChanges =
        uiState.initialWorkoutName != uiState.workoutName || uiState.initialExercises != uiState.exercises
    val hasPerformedSets = uiState.exercises.any { it.sets.any { set -> set.checked } }
    val hasChanges = hasUnsavedChanges || hasPerformedSets

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(hasChanges) {
        onNavigationGuardChange(hasChanges)
    }

    fun onFinishWorkout() {
        releaseNavigationGuard()
        viewModel.onFinishWorkoutPressed {
            onNavigateBack()
        }
    }

    fun finishWorkoutCheck() {
        if (hasPerformedSets && uiState.showFinishWorkoutDialog) {
            finishWorkoutDialogOpen = true
        } else {
            onFinishWorkout()
        }
    }

    fun saveChanges() {
        releaseNavigationGuard()
        viewModel.saveChanges()
        onNavigateBack()
    }

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopBarTextField(
                        value = uiState.workoutName,
                        onValueChange = viewModel::onWorkoutNameChange,
                        maxSize = WORKOUT_NAME_MAX_SIZE
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val enabled = hasChanges
            GymFloatingActionButton(
                enabled = enabled,
                onClick = {
                    if (hasPerformedSets)
                        finishWorkoutCheck()
                    else
                        saveChanges()
                }
            ) {
                if (hasPerformedSets) {
                    Icon(
                        painter = painterResource(id = R.drawable.goal),
                        contentDescription = stringResource(id = R.string.done)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.save),
                        contentDescription = stringResource(id = R.string.save)
                    )
                }
            }
        }
    ) { innerPadding ->
        GymWorkoutScreen(
            loading = uiState.loading,
            latestTimestamp = uiState.latestTimestamp,
            addingTimestamp = uiState.sessionTimestamp,
            exercises = uiState.exercises,
            addExercise = viewModel::addExercise,
            onRemoveExercise = viewModel::onRemoveExercise,
            onExerciseNameChange = viewModel::onExerciseNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            addSet = viewModel::addSet,
            onChangeWeight = viewModel::onChangeWeight,
            onChangeRepetitions = viewModel::onChangeRepetitions,
            onRemoveSet = viewModel::onRemoveSet,
            onCheckSet = viewModel::onCheckSet,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (statsBottomSheetOpen && uiState.stats != null) {
        ModalBottomSheet(
            onDismissRequest = { statsBottomSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            val weightUnitString = stringResource(id = UnitUtil.weightUnitStringId)

            Column {
                LazyColumn {
                    itemsIndexed(uiState.stats!!.exercises) { index, exercise ->
                        BasicLineChart(
                            title = {
                                Text(
                                    text = exercise.name.ifEmpty {
                                        "${stringResource(id = R.string.exercise)} ${index + 1}"
                                    }
                                )
                            },
                            bottomLabels =
                                if (exercise.setHistory.isNotEmpty()) {
                                    listOf(
                                        exercise.setHistory.first().timestamp.toDateString(),
                                        exercise.setHistory.last().timestamp.toDateString()
                                    )
                                } else
                                    emptyList(),
                            dataValues = exercise.setHistory.map { it.maxWeight },
                            popupContentBuilder = { dataIndex, valueIndex, value ->
                                "${exercise.setHistory[valueIndex].maxWeight} ${weightUnitString}\n ${exercise.setHistory[valueIndex].timestamp.toDateString()}"
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (finishWorkoutDialogOpen) {
        var doNotAskAgain by remember { mutableStateOf(false) }

        ConfirmDialog(
            subtitle = {
                Column {
                    Text(
                        text = stringResource(id = R.string.confirm_done_workout),
                        textAlign = TextAlign.Center
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            doNotAskAgain,
                            onCheckedChange = { doNotAskAgain = it }
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
                        if (doNotAskAgain) {
                            viewModel.stopAskingFinishConfirm()
                        }
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
fun GymWorkoutScreen(
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
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
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
                if (addingTimestamp != null) {
                    Text(
                        text = "${stringResource(id = R.string.adding_for_date)}: ${addingTimestamp.toDateString()}"
                    )
                } else {
                    Text(
                        text = "${stringResource(id = R.string.last_time)}: ${latestTimestamp?.toDateAndTimeString() ?: "-"}"
                    )
                }
            }
            ExerciseListEdit(
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
    GymWorkoutScreen(
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
private fun GymWorkoutPreview() {
    GymTrackerTheme {
        ScreenForPreview()
    }
}