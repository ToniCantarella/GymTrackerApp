package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.workouts.split.WorkoutSet

@Composable
fun Set(
    index: Int,
    set: WorkoutSet,
    onChangeWeight: (Double) -> Unit,
    onChangeRepetitions: (Int) -> Unit,
    onRemoveSet: () -> Unit,
    editing: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (editing) {
            Text(
                text = "${stringResource(id = R.string.set)} ${index + 1}",
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            Checkbox(
                checked = true,
                onCheckedChange = {}
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            var weight by remember { mutableStateOf(set.weight.toString()) }
            var repetitions by remember { mutableStateOf(set.repetitions.toString()) }

            if (editing) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        val newWeight = it.toDoubleOrNull()
                        if (newWeight != null) onChangeWeight(newWeight)
                        else onChangeWeight(0.0)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
                    modifier = Modifier.width(80.dp)
                )
            } else {
                Text(
                    text = set.weight.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = "kg",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
            if (editing) {
                OutlinedTextField(
                    value = repetitions,
                    onValueChange = {
                        repetitions = it
                        val newRepetitions = it.toIntOrNull()
                        if (newRepetitions != null) onChangeRepetitions(newRepetitions)
                        else onChangeRepetitions(0)
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
                    modifier = Modifier.width(60.dp)
                )
            } else {
                Text(
                    text = set.repetitions.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = stringResource(id = R.string.repetitions),
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (editing) {
            IconButton(
                onClick = { onRemoveSet() },
                enabled = index > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        } else {
            IconButton(
                onClick = { },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
}