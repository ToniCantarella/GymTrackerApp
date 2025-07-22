package com.example.gymtracker.ui.workouts.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.NumericTextField
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import com.example.gymtracker.utility.UnitUtil

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
            NumericTextField(
                value = set.weight,
                onValueChange = onChangeWeight
            )
            Text(
                text = stringResource(UnitUtil.weightUnitStringId),
                style = MaterialTheme.typography.labelMedium
            )
            NumericTextField(
                value = set.repetitions,
                onValueChange = onChangeRepetitions
            )
            Text(
                text = stringResource(id = R.string.repetitions_count),
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
                    enabled = deletionEnabled,
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