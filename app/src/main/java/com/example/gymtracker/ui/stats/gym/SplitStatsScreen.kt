package com.example.gymtracker.ui.stats.gym

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.ExerciseWithHistory
import com.example.gymtracker.database.repository.SplitStats
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.toDateString
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplitStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = uiState.splitName
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
        SplitStatsScreen(
            stats = uiState.stats!!
        )
    }
}

@Composable
private fun SplitStatsScreen(
    stats: SplitStats,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        itemsIndexed(stats.exercises) { index, exercise ->
            HorizontalDivider()
            WeightLineChart(
                exercise = if (exercise.name.isEmpty())
                    exercise.copy(
                        name = "${stringResource(id = R.string.exercise)} ${index + 1}"
                    )
                else exercise
            )
        }
    }
}

@Composable
fun WeightLineChart(
    exercise: ExerciseWithHistory,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary

    val rangePadding = 5.0
    val minValue = remember {
        exercise.setHistory.minBy { it.maxWeight }.maxWeight - rangePadding
    }
    val maxValue = remember {
        exercise.setHistory.maxBy { it.maxWeight }.maxWeight + rangePadding
    }

    val weightUnitString = stringResource(id = UnitUtil.weightUnitStringId)

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        Text(
            text = exercise.name
        )
        LineChart(
            modifier = Modifier.heightIn(max = 200.dp),
            data = remember {
                listOf(
                    Line(
                        label = "max",
                        values = exercise.setHistory.map { it.maxWeight }.ifEmpty { listOf(0.0) },
                        color = SolidColor(lineColor),
                        firstGradientFillColor = lineColor.copy(alpha = .5f),
                        secondGradientFillColor = Color.Transparent,
                        strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
                        gradientAnimationDelay = 500,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                    )
                )
            },
            minValue = minValue,
            maxValue = maxValue,
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.labelSmall,
                labels = if (exercise.setHistory.isNotEmpty()) {
                    listOf(
                        exercise.setHistory.first().timestamp.toDateString(),
                        exercise.setHistory.last().timestamp.toDateString()
                    )
                } else emptyList(),
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
            popupProperties = PopupProperties(
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                contentBuilder = { dataIndex, valueIndex, value ->
                    "${exercise.setHistory[valueIndex].maxWeight} ${weightUnitString}\n ${exercise.setHistory[valueIndex].timestamp.toDateString()}"
                }
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed(),
                    lineCount = 6
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = false
                )
            ),
            dividerProperties = DividerProperties(
                enabled = false
            ),
            animationMode = AnimationMode.Together(delayBuilder = { it * 500L })
        )
    }
}