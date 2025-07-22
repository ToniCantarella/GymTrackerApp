package com.example.gymtracker.ui.workouts.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.utility.MAX_EXERCISES
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    creatingSplit: Boolean,
    onAddExercise: () -> Unit,
    onRemoveExercise: (id: UUID) -> Unit,
    onExerciseNameChange: (id: UUID, name: String) -> Unit,
    onDescriptionChange: (id: UUID, description: String) -> Unit,
    onAddSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, weight: Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, repetitions: Int) -> Unit,
    onCheckSet: (exerciseId: UUID, setId: UUID, checked: Boolean) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Exercise?>(null) }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            vertical = dimensionResource(id = R.dimen.padding_large),
            horizontal = dimensionResource(id = R.dimen.padding_large)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .fillMaxSize()
    ) {
        itemsIndexed(exercises, key = { _, exercise -> exercise.uuid }) { index, exercise ->
            Exercise(
                exercise = exercise,
                placeholderName = "${stringResource(id = R.string.exercise)} ${index + 1}",
                onNameChange = { name -> onExerciseNameChange(exercise.uuid, name) },
                onDescriptionChange = { description ->
                    onDescriptionChange(
                        exercise.uuid,
                        description
                    )
                },
                onDelete = {
                    deleteDialogOpen = true
                    itemToDelete = exercise
                },
                addSet = { onAddSet(exercise.uuid) },
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
                onCheckSet = { setId, checked ->
                    onCheckSet(
                        exercise.uuid,
                        setId,
                        checked
                    )
                },
                creatingExercise = creatingSplit
            )
        }
        item {
            Button(
                onClick = {
                    onAddExercise()
                    scope.launch {
                        lazyListState.animateScrollToItem(exercises.size)
                    }
                },
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
    if (deleteDialogOpen) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(
                        id = R.string.exercise_deletion_subtitle,
                        itemToDelete?.name ?: stringResource(id = R.string.exercise)
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { onRemoveExercise(it.uuid) }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.delete)
                    )
                }
            },
            cancelButton = {
                OutlinedButton(
                    onClick = {
                        itemToDelete = null
                        deleteDialogOpen = false
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            onDismissRequest = {
                deleteDialogOpen = false
            }
        )
    }
}