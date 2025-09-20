package com.tonicantarella.gymtracker.ui.gym.creategymworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymDialog
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.common.UnsavedChangesDialog
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.gym.common.CreateExercise
import com.tonicantarella.gymtracker.ui.gym.common.ExerciseList
import com.tonicantarella.gymtracker.utility.WORKOUT_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGymWorkoutScreen(
    viewModel: CreateGymWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        viewModel.onNavigateBack()
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
                        onClick = {
                            viewModel.onNavigateBack()
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
            GymFloatingActionButton(
                enabled = enabled,
                onClick = viewModel::onCreateWorkoutPressed
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

    if (uiState.unSavedChangesDialogOpen) {
        UnsavedChangesDialog(
            onConfirm = viewModel::onConfirmUnsavedChangesDialog,
            onCancel = viewModel::dismissUnsavedChangesDialog
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
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = modifier
    ) {
        ExerciseList(
            exercises = exercises,
            addExercise = addExercise,
            modifier = Modifier
        ) { index, exercise, placeholderName ->
            CreateExercise(
                exercise = exercise,
                placeholderName = placeholderName,
                onNameChange = { name -> onExerciseNameChange(exercise.uuid, name) },
                onDescriptionChange = { description ->
                    onDescriptionChange(
                        exercise.uuid,
                        description
                    )
                },
                onDeletePressed = {
                    deleteDialogOpen = true
                    itemToDelete = exercise.copy(name = exercise.name.ifBlank { placeholderName })
                },
                deleteEnabled = exercises.size > 1,
                addSet = { addSet(exercise.uuid) },
                onChangeWeight = { setId, weight -> onChangeWeight(exercise.uuid, setId, weight) },
                onChangeRepetitions = { setId, repetitions ->
                    onChangeRepetitions(
                        exercise.uuid,
                        setId,
                        repetitions
                    )
                },
                onRemoveSet = { setId -> onRemoveSet(exercise.uuid, setId) },
            )
        }

        if (deleteDialogOpen && itemToDelete != null) {
            GymDialog(
                onDismissRequest = { deleteDialogOpen = false },
                title = {},
                subtitle = {
                    Text(
                        text = stringResource(
                            id = R.string.exercise_deletion_subtitle,
                            itemToDelete!!.name
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveExercise(itemToDelete!!.uuid)
                            deleteDialogOpen = false
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete)
                        )
                    }
                },
                onCancel = {
                    itemToDelete = null
                    deleteDialogOpen = false
                }
            )
        }
    }
}