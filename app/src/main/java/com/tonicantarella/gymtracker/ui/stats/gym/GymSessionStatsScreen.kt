package com.tonicantarella.gymtracker.ui.stats.gym

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.entity.gym.Exercise
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutSet
import com.tonicantarella.gymtracker.ui.entity.gym.WorkoutWithExercises
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymSessionStatsScreen(
    viewModel: GymSessionStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.workout?.name ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        GymSessionStatsScreen(
            loading = uiState.loading,
            split = uiState.workout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun GymSessionStatsScreen(
    loading: Boolean,
    split: WorkoutWithExercises?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (split != null && split.exercises.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
            ) {
                Text(
                    text = split.timestamp?.toDateAndTimeString() ?: "-"
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                contentPadding = PaddingValues(
                    dimensionResource(id = R.dimen.padding_large)
                )
            ) {
                itemsIndexed(split.exercises) { index, exercise ->
                    SessionExerciseCard(
                        exercise = exercise.copy(
                            name = exercise.name.ifBlank {
                                "${stringResource(id = R.string.exercise)} ${index + 1}"
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionExerciseCard(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.card_elevation)
        ),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )
            exercise.sets.forEachIndexed { index, set ->
                SessionSet(
                    set = set,
                    label = "${stringResource(id = R.string.set)} ${index + 1}"
                )
            }
        }
    }
}

@Composable
private fun SessionSet(
    set: WorkoutSet,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = " ${
                BigDecimal.valueOf(set.weight).stripTrailingZeros()
                    .toPlainString()
            } ${stringResource(UnitUtil.weightUnitStringId)}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
        Text(
            text = "${set.repetitions} ${stringResource(id = R.string.repetitions_count)}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    locale = "fi"
)
@Composable
private fun GymSessionPreview() {
    GymTrackerTheme {
        Surface {
            GymSessionStatsScreen(
                loading = false,
                split = WorkoutWithExercises(
                    id = 0,
                    name = "Split",
                    timestamp = Instant.now(),
                    exercises = listOf(
                        Exercise(
                            uuid = UUID.randomUUID(),
                            name = "Bench press",
                            description = "Description",
                            sets = listOf(
                                WorkoutSet(
                                    uuid = UUID.randomUUID(),
                                    weight = 10.0,
                                    repetitions = 10
                                ),
                                WorkoutSet(
                                    uuid = UUID.randomUUID(),
                                    weight = 10.01,
                                    repetitions = 10
                                ),
                                WorkoutSet(
                                    uuid = UUID.randomUUID(),
                                    weight = 10.120,
                                    repetitions = 10
                                )
                            )
                        ),
                        Exercise(
                            uuid = UUID.randomUUID(),
                            name = "",
                            description = "Description",
                            sets = listOf(
                                WorkoutSet(
                                    uuid = UUID.randomUUID(),
                                    weight = 10.0,
                                    repetitions = 10
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}