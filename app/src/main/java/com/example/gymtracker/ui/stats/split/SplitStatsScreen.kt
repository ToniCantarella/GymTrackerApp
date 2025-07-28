package com.example.gymtracker.ui.stats.split

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.ExerciseWithHistory
import com.example.gymtracker.database.repository.SplitStats
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
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
fun SplitStatsScreen(
    onNavigateBack: () -> Unit,
    viewmModel: SplitStatsViewModel = koinViewModel()
) {
    val uiState by viewmModel.uiState.collectAsState()

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
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .fillMaxSize()
    ) {
        itemsIndexed(stats.exercises) { index, exercise ->
            LineChartCard(
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
private fun LineChartCard(
    exercise: ExerciseWithHistory,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(
                text = exercise.name
            )
            LineChart(
                modifier = Modifier.heightIn(max = 300.dp),
                data = remember {
                    listOf(
                        Line(
                            label = "min",
                            values = exercise.setHistory.map { it.min },
                            color = SolidColor(Color(0xFF23af92)),
                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(700, easing = EaseInOutCubic),
                            gradientAnimationDelay = 700,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        ),
                        Line(
                            label = "max",
                            values = exercise.setHistory.map { it.max },
                            color = SolidColor(Color(0xFFAF239C)),
                            firstGradientFillColor = Color(0xFFC02BB1).copy(alpha = .5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
                            gradientAnimationDelay = 500,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                },
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall,
                    labels = if (exercise.setHistory.isNotEmpty()) {
                        listOf(
                            exercise.setHistory.first().timestamp.toDateString(),
                            "-",
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
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
