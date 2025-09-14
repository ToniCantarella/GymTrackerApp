package com.tonicantarella.gymtracker.ui.common

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.utility.toDateString
import kotlinx.coroutines.delay

@Composable
fun WorkoutList(
    workouts: List<WorkoutWithTimestamp>,
    selectingItems: Boolean,
    selectedItems: List<WorkoutWithTimestamp>,
    onSelect: (workout: WorkoutWithTimestamp) -> Unit,
    onClick: (workout: WorkoutWithTimestamp) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(
            vertical = dimensionResource(id = R.dimen.padding_medium),
            horizontal = dimensionResource(id = R.dimen.padding_large)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(workouts) { index, workout ->
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            val offsetX by animateDpAsState(
                targetValue = if (visible) 0.dp else 200.dp,
                animationSpec = tween(durationMillis = 300)
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(durationMillis = 300)
            )

            Box(
                modifier = Modifier.widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
            ) {
                WorkoutListItem(
                    workout = workout,
                    selectingItems = selectingItems,
                    selected = workout in selectedItems,
                    onSelect = onSelect,
                    onClick = onClick,
                    modifier = Modifier
                        .offset(x = offsetX)
                        .alpha(alpha)
                )
            }
        }
    }
}

@Composable
fun WorkoutListItem(
    workout: WorkoutWithTimestamp,
    onClick: (workout: WorkoutWithTimestamp) -> Unit,
    modifier: Modifier = Modifier,
    selectingItems: Boolean = false,
    selected: Boolean = false,
    onSelect: (workout: WorkoutWithTimestamp) -> Unit = {}
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = dimensionResource(id = R.dimen.card_elevation)
        ),
        enabled = !selectingItems,
        onClick = {
            if (!selectingItems) onClick(workout)
        },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.history),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = stringResource(id = R.string.last_time),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = workout.timestamp?.toDateString() ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectingItems,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { onSelect(workout) }
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = !selectingItems,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.keyboard_arrow_right),
                        contentDescription = stringResource(R.string.select),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutListItemPreview() {
    GymTrackerTheme {
        WorkoutListItem(
            workout = WorkoutWithTimestamp(
                id = 0,
                name = "Workout 1",
                timestamp = null
            ),
            selectingItems = false,
            selected = false,
            onSelect = {},
            onClick = {}
        )
    }
}