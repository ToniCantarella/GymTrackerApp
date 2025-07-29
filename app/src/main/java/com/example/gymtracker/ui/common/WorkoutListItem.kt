package com.example.gymtracker.ui.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.WorkoutListItem
import com.example.gymtracker.utility.toDateAndTimeString

@Composable
fun WorkoutListItem(
    workout: WorkoutListItem,
    selectingItems: Boolean,
    selected: Boolean,
    onSelect: (id: Int) -> Unit,
    onHold: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!selectingItems) onClick() },
                onLongClick = onHold
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${stringResource(id = R.string.last_time)}: ${workout.latestTimestamp?.toDateAndTimeString() ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectingItems) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = {
                        onSelect(workout.id)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}