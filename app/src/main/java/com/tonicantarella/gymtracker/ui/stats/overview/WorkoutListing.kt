package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp

@Composable
fun WorkoutListing(
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
    onGymWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    onCardioWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    gymColorIndexMap: Map<Int, Int>,
    cardioColorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
    ) {
        if (gymWorkouts.isNotEmpty()) {
            gymWorkouts.forEachIndexed { index, workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { onGymWorkoutStatsNavigate(workout.id) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.weight),
                            contentDescription = null,
                            tint = highlightColors[gymColorIndexMap[workout.id] ?: 0]
                        )
                    }
                )
            }
        }
        if (cardioWorkouts.isNotEmpty()) {
            cardioWorkouts.forEachIndexed { index, workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { onCardioWorkoutStatsNavigate(workout.id) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.run),
                            contentDescription = null,
                            tint = highlightColors[cardioColorIndexMap[workout.id] ?: 0]
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutWithTimestamp,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.padding_medium)
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                icon()
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = workout.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.keyboard_arrow_right),
                contentDescription = stringResource(R.string.select),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}