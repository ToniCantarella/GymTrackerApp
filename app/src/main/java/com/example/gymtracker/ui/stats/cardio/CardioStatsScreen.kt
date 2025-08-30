package com.example.gymtracker.ui.stats.cardio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.repository.CardioStats
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.stats.BasicLineChart
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.toDateString
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardioStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = uiState.stats?.name ?: ""
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

    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.stats != null) {
        CardioStatsScreen(
            stats = uiState.stats!!
        )
    }
}

@Composable
private fun CardioStatsScreen(
    stats: CardioStats,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            val stepHistory = stats.cardioHistory.filter { it.steps != null }
            val stepValues = stepHistory.map { it.steps!!.toDouble() }

            HorizontalDivider()
            BasicLineChart(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.footprint),
                            contentDescription = stringResource(id = R.string.steps)
                        )
                        Text(
                            text = stringResource(id = R.string.steps)
                        )
                    }
                },
                bottomLabels = if (stepHistory.isNotEmpty()) {
                    listOf(
                        stepHistory.first().timestamp?.toDateString() ?: "",
                        stepHistory.last().timestamp?.toDateString() ?: "",
                    )
                } else emptyList(),
                dataValues = stepValues,
                popupContentBuilder = { dataIndex, valueIndex, value ->
                    "${stepHistory[valueIndex].steps}\n ${stepHistory[valueIndex].timestamp?.toDateString()}"
                },
            )
        }
        item {
            val distanceHistory = stats.cardioHistory.filter { it.distance != null }
            val distanceValues = distanceHistory.map { it.distance!! }

            HorizontalDivider()
            BasicLineChart(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.path),
                            contentDescription = stringResource(id = R.string.distance)
                        )
                        Text(
                            text = "${stringResource(id = R.string.distance)} (${
                                stringResource(
                                    UnitUtil.distanceUnitStringId
                                )
                            })"
                        )
                    }
                },
                bottomLabels = if (distanceHistory.isNotEmpty()) {
                    listOf(
                        distanceHistory.first().timestamp?.toDateString() ?: "",
                        distanceHistory.last().timestamp?.toDateString() ?: "",
                    )
                } else emptyList(),
                dataValues = distanceValues,
                popupContentBuilder = { dataIndex, valueIndex, value ->
                    "${distanceHistory[valueIndex].distance}\n ${distanceHistory[valueIndex].timestamp?.toDateString()}"
                }
            )
        }
        item {
            val durationHistory = stats.cardioHistory.filter { it.duration != null }
            val rawDurations = durationHistory.map { it.duration!! }

            val maxMillis = rawDurations.maxOfOrNull { it.toMillis() } ?: 0L
            val (divider, unitLabel) = when {
                maxMillis >= 3_600_000 -> 3_600_000.0 to "h"
                maxMillis >= 60_000 -> 60_000.0 to "min"
                else -> 1_000.0 to "s"
            }

            val durationValues = rawDurations.map { it.toMillis() / divider }

            HorizontalDivider()
            BasicLineChart(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timer),
                            contentDescription = stringResource(id = R.string.time)
                        )
                        Text(
                            text = "${stringResource(id = R.string.time)} (${unitLabel})"
                        )
                    }
                },
                dataValues = durationValues,
                bottomLabels = if (durationHistory.isNotEmpty()) {
                    listOf(
                        durationHistory.first().timestamp?.toDateString() ?: "",
                        durationHistory.last().timestamp?.toDateString() ?: "",
                    )
                } else emptyList(),
                popupContentBuilder = { dataIndex, valueIndex, value ->
                    "${durationHistory[valueIndex].duration?.toMillis()?.div(divider)}\n ${durationHistory[valueIndex].timestamp?.toDateString()}"
                }
            )
        }
    }
}