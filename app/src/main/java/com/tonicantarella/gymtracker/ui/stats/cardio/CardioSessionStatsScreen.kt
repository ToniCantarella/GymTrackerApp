package com.tonicantarella.gymtracker.ui.stats.cardio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.toDateAndTimeString
import com.tonicantarella.gymtracker.utility.toReadableString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioSessionStatsScreen(
    viewModel: CardioSessionStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.cardio?.name ?: ""
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
        CardioSessionStatsScreen(
            loading = uiState.loading,
            cardio = uiState.cardio,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun CardioSessionStatsScreen(
    loading: Boolean,
    cardio: WorkoutWithMetrics?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (cardio != null) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .pointerInput(Unit) {}
                )
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                    ) {
                        Text(
                            text = cardio.timestamp?.toDateAndTimeString() ?: "-"
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(id = R.dimen.padding_large))
                    ) {
                        SessionExerciseCard(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.footprint),
                                    contentDescription = null
                                )
                            },
                            label = stringResource(id = R.string.steps),
                            value = {
                                Text(
                                    text = "${cardio.metrics.steps} ${stringResource(id = R.string.steps)}"
                                )
                            }
                        )
                        SessionExerciseCard(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.path),
                                    contentDescription = null
                                )
                            },
                            label = stringResource(id = R.string.distance),
                            value = {
                                Text(
                                    text = "${cardio.metrics.distance} ${stringResource(UnitUtil.distanceUnitStringId)}"
                                )
                            }
                        )
                        SessionExerciseCard(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.timer),
                                    contentDescription = null
                                )
                            },
                            label = stringResource(id = R.string.time),
                            value = {
                                Text(
                                    text = cardio.metrics.duration?.toReadableString() ?: ""
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionExerciseCard(
    icon: @Composable () -> Unit,
    label: String,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.card_elevation)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            icon()
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = label
            )
            Spacer(modifier = Modifier.weight(1f))
            value()
        }
    }
}