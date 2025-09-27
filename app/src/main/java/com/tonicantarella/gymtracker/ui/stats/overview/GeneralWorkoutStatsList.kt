package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.statsoverview.CardioWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.stats.common.CardioGeneralStats
import com.tonicantarella.gymtracker.ui.stats.common.GymGeneralStats

@Composable
fun GeneralWorkoutStatsList(
    gymWorkouts: List<GymWorkoutWithGeneralStats>,
    cardioWorkouts: List<CardioWorkoutWithGeneralStats>,
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
                GymStatCard(
                    workout = workout,
                    gymColorIndexMap = gymColorIndexMap,
                    onDetailsClick = { onGymWorkoutStatsNavigate(workout.id) }
                )
            }
        }
        if (cardioWorkouts.isNotEmpty()) {
            cardioWorkouts.forEachIndexed { index, workout ->
                CardioStatCard(
                    workout = workout,
                    cardioColorIndexMap = cardioColorIndexMap,
                    onDetailsClick = { onCardioWorkoutStatsNavigate(workout.id) }
                )
            }
        }
    }
}

@Composable
private fun GymStatCard(
    workout: GymWorkoutWithGeneralStats,
    onDetailsClick: () -> Unit,
    gymColorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    WorkoutStatCard(
        workoutName = workout.name,
        onDetailsClick = onDetailsClick,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.dumbbell),
                contentDescription = null,
                tint = highlightColors[gymColorIndexMap[workout.id] ?: 0]
            )
        },
        modifier = modifier
    ) {
        GymGeneralStats(
            stats = workout
        )
    }
}

@Composable
private fun CardioStatCard(
    workout: CardioWorkoutWithGeneralStats,
    onDetailsClick: () -> Unit,
    cardioColorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    WorkoutStatCard(
        workoutName = workout.name,
        onDetailsClick = onDetailsClick,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.run),
                contentDescription = null,
                tint = highlightColors[cardioColorIndexMap[workout.id] ?: 0]
            )
        },
        modifier = modifier
    ) {
        CardioGeneralStats(
            stats = workout
        )
    }
}

@Composable
private fun WorkoutStatCard(
    workoutName: String,
    icon: @Composable () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.padding_medium)
        ),
        modifier = modifier
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_large)
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    icon()
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                    Text(
                        text = workoutName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    onClick = onDetailsClick,
                    modifier = Modifier
                ) {
                    Text(
                        text = stringResource(id = R.string.details)
                    )
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = R.drawable.keyboard_arrow_right),
                        contentDescription = null
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_large))
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}