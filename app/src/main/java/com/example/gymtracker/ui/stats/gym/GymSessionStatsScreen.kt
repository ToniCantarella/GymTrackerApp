package com.example.gymtracker.ui.stats.gym

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.repository.SplitWithExercises
import com.example.gymtracker.ui.gym.entity.Exercise
import com.example.gymtracker.ui.gym.entity.WorkoutSet
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Composable
fun GymSessionStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GymSessionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = uiState.split?.name ?: ""
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )

    GymSessionStatsScreen(
        loading = uiState.loading,
        split = uiState.split
    )
}

@Composable
fun GymSessionStatsScreen(
    loading: Boolean,
    split: SplitWithExercises?,
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
            LazyColumn() {
                itemsIndexed(split.exercises) { index, exercise ->
                    HorizontalDivider()
                    Surface {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Text(
                                text = exercise.name.ifBlank { "${stringResource(id = R.string.exercise)} ${index + 1}" },
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                            exercise.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (index % 2 == 0)
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else
                                                MaterialTheme.colorScheme.surface
                                        )
                                ) {
                                    Text(
                                        text = "${stringResource(id = R.string.set)} ${index + 1}",
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
                        }
                    }
                }
            }
        }
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
                split = SplitWithExercises(
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