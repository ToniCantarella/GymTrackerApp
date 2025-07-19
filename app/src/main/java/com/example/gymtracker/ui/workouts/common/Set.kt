package com.example.gymtracker.ui.workouts.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.workouts.entity.WorkoutSet

@Composable
fun Set(
    set: WorkoutSet,
    onChangeWeight: (Double) -> Unit,
    onChangeRepetitions: (Int) -> Unit,
    onRemoveSet: () -> Unit,
    onCheckSet: (checked: Boolean) -> Unit,
    setName: String = "",
    addingSet: Boolean = false,
    deletionEnabled: Boolean = true
) {
    var dropdownMenuOpen by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
    ) {
        if (addingSet) {
            Text(
                text = setName
            )
        } else {
            Checkbox(
                checked = set.checked,
                onCheckedChange = onCheckSet
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            var weight by remember { mutableStateOf(set.weight.toString()) }
            var repetitions by remember { mutableStateOf(set.repetitions.toString()) }

            OutlinedTextField(
                value = weight,
                onValueChange = {
                    weight = it
                    onChangeWeight(it.toDoubleOrNull() ?: 0.0)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
                modifier = Modifier
                    .width(80.dp)
                    .height(45.dp)
            )
            Text(
                text = "kg",
                style = MaterialTheme.typography.labelMedium
            )
            OutlinedTextField(
                value = repetitions,
                onValueChange = {
                    repetitions = it
                    onChangeRepetitions(it.toIntOrNull() ?: 0)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.End),
                modifier = Modifier
                    .width(60.dp)
                    .height(45.dp)
            )
            Text(
                text = stringResource(id = R.string.repetitions),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Box {
            IconButton(
                onClick = { dropdownMenuOpen = !dropdownMenuOpen },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = dropdownMenuOpen,
                onDismissRequest = { dropdownMenuOpen = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.delete)
                        )
                    },
                    onClick = {
                        dropdownMenuOpen = false
                        onRemoveSet()
                    }
                )
            }
        }
    }
}