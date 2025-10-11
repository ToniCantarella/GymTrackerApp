package com.tonicantarella.gymtracker.ui.stats.overview

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutLegend
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun WorkoutPieChart(
    legends: List<WorkoutLegend>,
    workoutType: WorkoutType,
    colorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    var data by remember {
        mutableStateOf(
            if (legends.isNotEmpty() && legends.any { it.sessionCount > 0 }) {
                legends.mapIndexed { index, legend ->
                    val colorIndex = colorIndexMap[legend.workout.id] ?: 0
                    val color = highlightColors[colorIndex]

                    Pie(
                        label = legend.workout.name,
                        data = legend.sessionCount.toDouble(),
                        color = color
                    )
                }
            } else {
                listOf(
                    Pie(
                        label = "",
                        data = 1.0,
                        color = Color.Gray
                    )
                )
            }
        )
    }

    Box(modifier = modifier.fillMaxHeight()) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimensionResource(id = R.dimen.padding_medium)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(.8f)
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                WorkoutLegendsRow(
                    legends = legends,
                    colorIndexMap = colorIndexMap
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = "${stringResource(id = R.string.total)}: ${legends.sumOf { it.sessionCount }}"
                )
            }
        }
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
        ) {
            BlurredDonutShadow()
            Icon(
                painter =
                    if (workoutType == WorkoutType.GYM)
                        painterResource(id = R.drawable.dumbbell)
                    else
                        painterResource(id = R.drawable.run),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
            PieChart(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp),
                data = data,
                spaceDegree = 2f,
                style = Pie.Style.Stroke(width = 20.dp)
            )
        }
    }
}

@Composable
fun BlurredDonutShadow() {
    Canvas(modifier = Modifier.size(220.dp)) {
        val outerRadius = size.minDimension / 2
        val innerRadius = outerRadius * 0.7f
        val blurRadius = 40f

        val paint = Paint().apply {
            color = Color.Black.copy(alpha = 0.25f)
        }

        drawIntoCanvas { canvas ->
            val frameworkPaint = paint.asFrameworkPaint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }

            val outer = Rect(
                center.x - outerRadius, center.y - outerRadius,
                center.x + outerRadius, center.y + outerRadius
            )
            val inner = Rect(
                center.x - innerRadius, center.y - innerRadius,
                center.x + innerRadius, center.y + innerRadius
            )

            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(outer)
                addOval(inner)
            }

            canvas.drawPath(path, paint)
            frameworkPaint.maskFilter = null
        }
    }
}