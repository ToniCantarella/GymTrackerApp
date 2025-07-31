package com.example.gymtracker.ui.gym.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.utility.MAX_EXERCISES
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    modifier: Modifier = Modifier
) {
    ExerciseLazyColumn(
        exercises = exercises,
        modifier = modifier
    ) { index, exercise, placeholderName ->
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f)
                    .pointerInput(Unit) {}
            )
            Exercise(
                exercise = exercise,
                placeholderName = placeholderName
            )
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    modifier: Modifier = Modifier,
    onAddExercise: () -> Unit,
    onRemoveExercise: (id: UUID) -> Unit,
    onExerciseNameChange: (id: UUID, name: String) -> Unit,
    onDescriptionChange: (id: UUID, description: String) -> Unit,
    onAddSet: (exerciseId: UUID) -> Unit = { },
    onChangeWeight: (exerciseId: UUID, setId: UUID, weight: Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, repetitions: Int) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit,
    onCheckSet: (exerciseId: UUID, setId: UUID, checked: Boolean) -> Unit = { _, _, _ -> },
    creatingSplit: Boolean = false
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Exercise?>(null) }

    ExerciseLazyColumn(
        exercises = exercises,
        modifier = modifier
    ) { index, exercise, placeholderName ->
        Exercise(
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
            addSet = { onAddSet(exercise.uuid) },
            onChangeWeight = { setId, weight -> onChangeWeight(exercise.uuid, setId, weight) },
            onChangeRepetitions = { setId, repetitions ->
                onChangeRepetitions(
                    exercise.uuid,
                    setId,
                    repetitions
                )
            },
            onRemoveSet = { setId -> onRemoveSet(exercise.uuid, setId) },
            onCheckSet = { setId, checked -> onCheckSet(exercise.uuid, setId, checked) },
            creatingExercise = creatingSplit
        )

        if (index == exercises.lastIndex) {
            Button(
                onClick = {
                    onAddExercise()
                    scope.launch {
                        lazyListState.animateScrollToItem(exercises.size)
                    }
                },
                enabled = exercises.last().name.isNotEmpty() && exercises.size < MAX_EXERCISES,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
                Text(
                    text = stringResource(id = R.string.exercise)
                )
            }
        }
    }

    if (deleteDialogOpen && itemToDelete != null) {
        ConfirmDialog(
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

@Composable
private fun ExerciseLazyColumn(
    exercises: List<Exercise>,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int, exercise: Exercise, placeholderName: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            vertical = dimensionResource(id = R.dimen.padding_large),
            horizontal = dimensionResource(id = R.dimen.padding_large)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .fillMaxSize()
    ) {
        itemsIndexed(exercises, key = { _, exercise -> exercise.uuid }) { index, exercise ->
            val placeholderName = "${stringResource(id = R.string.exercise)} ${index + 1}"

            content(index, exercise, placeholderName)
        }
    }
}