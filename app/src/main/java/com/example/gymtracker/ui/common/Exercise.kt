package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.ui.workouts.MAX_SETS
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import java.util.UUID

@Composable
fun Exercise(
    index: Int,
    exercise: Exercise,
    onNameChange: (name: String) -> Unit,
    onDescriptionChange: (description: String) -> Unit,
    addSet: () -> Unit,
    onChangeWeight: (setId: UUID, Double) -> Unit,
    onChangeRepetitions: (setId: UUID, Int) -> Unit,
    onRemoveSet: (setId: UUID) -> Unit,
    onCheckSet: (setId: UUID, checked: Boolean) -> Unit,
    addingExercise: Boolean = false
) {
    var editingExercise by remember { mutableStateOf(addingExercise) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (editingExercise) {
                    OutlinedTextField(
                        value = exercise.name,
                        onValueChange = onNameChange,
                        placeholder = {
                            Text(
                                text = "${stringResource(id = R.string.exercise)} $index"
                            )
                        }
                    )
                } else {
                    Text(
                        text = exercise.name.ifEmpty { "${stringResource(id = R.string.exercise)} $index" }
                    )
                }
                if (!addingExercise) {
                    IconButton(
                        onClick = {
                            editingExercise = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null
                        )
                    }
                }
            }
            if (editingExercise) {
                OutlinedTextField(
                    value = exercise.description ?: "",
                    onValueChange = onDescriptionChange,
                    placeholder = {
                        Text(
                            text = "${stringResource(id = R.string.description)} (${
                                stringResource(
                                    id = R.string.optional
                                )
                            })",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                )
            } else {
                Text(
                    text = exercise.description ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Column {
                exercise.sets.forEachIndexed { index, set ->
                    HorizontalDivider()
                    Set(
                        index = index,
                        set = set,
                        onChangeWeight = { onChangeWeight(set.uuid, it) },
                        onChangeRepetitions = { onChangeRepetitions(set.uuid, it) },
                        onRemoveSet = { onRemoveSet(set.uuid) },
                        onCheckSet = { checked ->
                            onCheckSet(
                                set.uuid,
                                checked
                            )
                        },
                        addingSet = addingExercise
                    )
                }
                TextButton(
                    onClick = addSet,
                    enabled = exercise.sets.size < MAX_SETS
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.set)
                    )
                }
            }
        }
    }
}

val testExercise = Exercise(
    uuid = UUID.randomUUID(),
    name = "Bicep curls",
    description = "Remember to warm up!",
    sets = listOf(
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        ),
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        ),
        WorkoutSet(
            uuid = UUID.randomUUID(),
            weight = 10.0,
            repetitions = 8
        )
    )
)

@Composable
fun ExerciseForPreview(
    exercise: Exercise = testExercise,
    addingExercise: Boolean = false
) {
    Exercise(
        index = 0,
        exercise = exercise,
        onNameChange = {},
        onDescriptionChange = {},
        addSet = {},
        onChangeWeight = { _, _ -> },
        onChangeRepetitions = { _, _ -> },
        onRemoveSet = {},
        onCheckSet = { _, _ -> },
        addingExercise = addingExercise
    )
}

@Preview
@Composable
private fun ExercisePreview() {
    GymTrackerTheme {
        ExerciseForPreview()
    }
}

@Preview
@Composable
private fun AddingExercisePreview() {
    GymTrackerTheme {
        ExerciseForPreview(
            exercise = Exercise(
                uuid = UUID.randomUUID(),
                name = "",
                description = null,
                sets = listOf(
                    WorkoutSet(
                        uuid = UUID.randomUUID(),
                        weight = 0.0,
                        repetitions = 0
                    )
                )
            ),
            addingExercise = true
        )
    }
}