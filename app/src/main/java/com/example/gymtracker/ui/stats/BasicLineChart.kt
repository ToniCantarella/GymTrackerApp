package com.example.gymtracker.ui.stats

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
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

@Composable
fun BasicLineChart(
    title: @Composable () -> Unit,
    bottomLabels: List<String>,
    dataValues: List<Double>,
    popupContentBuilder: (dataIndex: Int, valueIndex: Int, value: Double) -> String,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary

    val rangePadding = 5.0
    val minValue = remember {
        val min = dataValues.minOrNull() ?: rangePadding
        if (min - rangePadding < 0)
            0.0
        else
            (min - rangePadding)
    }
    val maxValue = remember {
        val max = dataValues.maxOrNull() ?: rangePadding
        max + rangePadding
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        title()
        LineChart(
            modifier = Modifier.heightIn(max = 200.dp),
            data = remember {
                listOf(
                    Line(
                        label = "max",
                        values = dataValues.ifEmpty { listOf(0.0) },
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
                labels = bottomLabels.ifEmpty { emptyList() },
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
                contentBuilder = popupContentBuilder
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