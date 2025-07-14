package com.example.gymtracker.ui.workouts.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.Exercise
import com.example.gymtracker.ui.workouts.MAX_EXERCISES
import com.example.gymtracker.ui.workouts.entity.Exercise
import java.util.UUID

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    addingSplit: Boolean,
    onAddExercise: () -> Unit,
    onExerciseNameChange: (exerciseId: UUID, name: String) -> Unit,
    onDescriptionChange: (exerciseId: UUID, description: String) -> Unit,
    onAddSet: (exerciseId: UUID) -> Unit,
    onChangeWeight: (exerciseId: UUID, setId: UUID, Double) -> Unit,
    onChangeRepetitions: (exerciseId: UUID, setId: UUID, Int) -> Unit,
    onCheckSet: (exerciseId: UUID, setId: UUID, checked: Boolean) -> Unit,
    onRemoveSet: (exerciseId: UUID, setId: UUID) -> Unit,
    modifier: Modifier = Modifier
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
                addingExercise = addingSplit
            )
        }
        item {
            Button(
                onClick = onAddExercise,
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