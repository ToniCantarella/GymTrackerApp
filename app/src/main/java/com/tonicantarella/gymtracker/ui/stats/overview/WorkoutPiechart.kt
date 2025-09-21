package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
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

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.padding_medium)
        ),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
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
            WorkoutLegendsRow(
                legends = legends,
                colorIndexMap = colorIndexMap
            )
            Text(
                text = "${stringResource(id = R.string.total)}: ${legends.sumOf { it.sessionCount }}"
            )
        }
    }
}