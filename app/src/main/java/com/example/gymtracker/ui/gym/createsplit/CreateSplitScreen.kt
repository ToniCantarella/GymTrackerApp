package com.example.gymtracker.ui.gym.createsplit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.gym.common.ExerciseListCreate
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideNavigationBarGuard
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.navigation.rememberProceedOnGuardCleared
import com.example.gymtracker.utility.SPLIT_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun CreateSplitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateSplitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var backNavigationDialog by remember { mutableStateOf(false) }
    var dialogNavigationAction: () -> Unit by remember { mutableStateOf({}) }
    val hasUnsavedChanges = uiState.splitName != "" || uiState.exercises != uiState.initialExercises

    fun navigationCheck(onNavigate: () -> Unit) {
        if (hasUnsavedChanges) {
            backNavigationDialog = true
            dialogNavigationAction = onNavigate
        } else {
            onNavigate()
        }
    }

    BackHandler {
        navigationCheck { onNavigateBack() }
    }

    ProvideNavigationBarGuard(
        isGuarded = hasUnsavedChanges,
        onGuard = { backNavigationDialog = true }
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
        }
    )

    ProvideFloatingActionButton(
        onClick = { viewModel.onCreateSplitPressed { onNavigateBack() } },
        enabled = uiState.exercises.last().name.isNotEmpty() && uiState.splitName.isNotEmpty()
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

    if (backNavigationDialog) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(id = R.string.unsaved_changes),
                    textAlign = TextAlign.Center
                )
            },
            cancelButton = {
                OutlinedButton(
                    onClick = { backNavigationDialog = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backNavigationDialog = false
                        dialogNavigationAction.invoke()
                        proceedOnGuardCleared()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            onDismissRequest = { backNavigationDialog = false }
        )
    }
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
        ExerciseListCreate(
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