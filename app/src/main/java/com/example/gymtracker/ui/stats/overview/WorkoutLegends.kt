package com.example.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.entity.statsoverview.Workout
import com.example.gymtracker.ui.entity.statsoverview.WorkoutLegend
import com.example.gymtracker.ui.entity.statsoverview.WorkoutType

@Composable
fun WorkoutLegendsRow(
    legends: List<WorkoutLegend>,
    colorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val maxItemsInRow = 3
    FlowRow(
        maxItemsInEachRow = maxItemsInRow,
        modifier = modifier.fillMaxWidth()
    ) {
        legends.forEachIndexed { index, legend ->
            val colorIndex = colorIndexMap[legend.workout.id] ?: 0

            WorkoutLegend(
                workout = legend.workout,
                sessionCount = legend.sessionCount,
                highlightColor = highlightColors[colorIndex],
                modifier = Modifier.weight(1f)
            )
        }
        if (legends.size % maxItemsInRow != 0) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun WorkoutLegend(
    workout: Workout,
    sessionCount: Int,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        WorkoutIcon(
            painter =
                if (workout.type == WorkoutType.GYM)
                    painterResource(id = R.drawable.weight)
                else
                    painterResource(id = R.drawable.run),
            tint = highlightColor
        )
        Text(
            text = "$sessionCount x",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = workout.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WorkoutIcon(
    painter: Painter,
    tint: Color? = null
) {
    Icon(
        painter = painter,
        tint = tint ?: MaterialTheme.colorScheme.onSurface,
        contentDescription = stringResource(id = R.string.icon),
        modifier = Modifier.size(16.dp)
    )
}