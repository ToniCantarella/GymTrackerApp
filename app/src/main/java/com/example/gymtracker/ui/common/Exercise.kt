package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.workouts.split.Exercise
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
    editing: Boolean = false
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            if (editing) {
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = onNameChange,
                    placeholder = {
                        Text(
                            text = "${stringResource(id = R.string.exercise)} $index"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            } else {
                Text(
                    text = exercise.name
                )
            }
            if (editing) {
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
                        editing = editing
                    )
                }
                if (editing) {
                    TextButton(
                        onClick = addSet
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
}