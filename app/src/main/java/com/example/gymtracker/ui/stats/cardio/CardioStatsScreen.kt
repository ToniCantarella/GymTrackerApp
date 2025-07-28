package com.example.gymtracker.ui.stats.cardio

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.CardioStats
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.toDateString
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
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
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            val stepHistory = stats.cardioHistory.filter { it.steps != null }
            val stepValues = stepHistory.map { it.steps!!.toDouble() }

            LineChartCard(
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
                values = stepValues,
                dateLabels = listOf(
                    stepHistory.first().timestamp?.toDateString() ?: "",
                    "-",
                    stepHistory.last().timestamp?.toDateString() ?: "",
                )
            )
        }
        item {
            val distanceHistory = stats.cardioHistory.filter { it.distance != null }
            val distanceValues = distanceHistory.map { it.distance!! }

            LineChartCard(
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
                            text = "${stringResource(id = R.string.distance)} (${stringResource(UnitUtil.distanceUnitStringId)})"
                        )
                    }
                },
                values = distanceValues,
                dateLabels = listOf(
                    distanceHistory.first().timestamp?.toDateString() ?: "",
                    "-",
                    distanceHistory.last().timestamp?.toDateString() ?: "",
                )
            )
        }
        item {
            val durationHistory = stats.cardioHistory.filter { it.duration != null }
            val rawDurations = durationHistory.map { it.duration!! }

            val maxMillis = rawDurations.maxOfOrNull { it.toMillis() } ?: 0L
            val (divider, unitLabel) = when {
                maxMillis >= 3_600_000 -> 3_600_000.0 to "h"
                maxMillis >= 60_000    -> 60_000.0 to "min"
                else                   -> 1_000.0 to "s"
            }

            val durationValues = rawDurations.map { it.toMillis() / divider }

            LineChartCard(
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
                values = durationValues,
                dateLabels = listOf(
                    durationHistory.first().timestamp?.toDateString() ?: "",
                    "-",
                    durationHistory.last().timestamp?.toDateString() ?: "",
                )
            )
        }
    }
}

@Composable
private fun LineChartCard(
    title: @Composable () -> Unit,
    values: List<Double>,
    dateLabels: List<String>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            title()
            LineChart(
                modifier = Modifier.heightIn(max = 300.dp),
                data = remember {
                    listOf(
                        Line(
                            label = "",
                            values = values,
                            color = SolidColor(Color(0xFF23af92)),
                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(700, easing = EaseInOutCubic),
                            gradientAnimationDelay = 700,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                },
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall,
                    labels = dateLabels,
                    builder = { modifier, label, shouldRotate, index ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                ),
                labelHelperProperties = LabelHelperProperties(
                    enabled = false
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                ),
                animationMode = AnimationMode.Together(delayBuilder = { it * 500L })
            )
        }
    }
}