package com.tonicantarella.gymtracker.ui.stats

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.gym.SetStats
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import java.time.Instant

@Composable
fun BasicBarChart(
    title: @Composable () -> Unit,
    bottomLabels: List<String>,
    dataValues: List<SetStats>,
    popupContentBuilder: (dataIndex: Int, valueIndex: Int, value: Double) -> String,
    modifier: Modifier = Modifier
) {
    val minColor = MaterialTheme.colorScheme.tertiary
    val maxColor = MaterialTheme.colorScheme.primary
    val minText = stringResource(id = R.string.min)
    val maxText = stringResource(id = R.string.max)

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        title()

        ColumnChart(
            modifier = Modifier.heightIn(max = 200.dp),
            data = remember {
                dataValues.map { data ->
                    Bars(
                        label = "Jan",
                        values = listOf(
                            Bars.Data(
                                label = maxText,
                                value = data.maxRepetitions.toDouble(),
                                color = SolidColor(maxColor)
                            ),
                            Bars.Data(
                                label = minText,
                                value = data.minRepetitions.toDouble(),
                                color = SolidColor(minColor)
                            )
                        ),
                    )
                }
            },
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.labelSmall,
                labels = bottomLabels.ifEmpty { emptyList() },
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
                contentBuilder = {
                    "${it.toInt()}"
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
            popupProperties = PopupProperties(
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                contentBuilder = popupContentBuilder
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            animationMode = AnimationMode.Together(delayBuilder = { it * 10L }),
            dividerProperties = DividerProperties(
                enabled = false
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BarChartPreview() {
    GymTrackerTheme {
        BasicBarChart(
            title = {
                Text(
                    text = "This is the title"
                )
            },
            bottomLabels = listOf("Day 1", "Day last"),
            dataValues = listOf(
                SetStats(
                    minWeight = 1.0,
                    maxWeight = 2.0,
                    minRepetitions = 1,
                    maxRepetitions = 2,
                    timestamp = Instant.now()
                )
            ),
            popupContentBuilder = { _, _, _ -> "" }
        )
    }
}