package com.tonicantarella.gymtracker.ui.stats.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.utility.UnitUtil

@Composable
fun GymGeneralStats(
    stats: GymWorkoutWithGeneralStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        LabelValueRow(
            label = "${stringResource(id = R.string.exercises)}:",
            value = "${stats.exerciseCount}"
        )
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_sets)}:",
            value = "${stats.avgSets}"
        )
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_reps)}:",
            value = "${stats.avgReps}"
        )
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_weight)}:",
            value = "${stats.avgWeight} ${stringResource(UnitUtil.weightUnitStringId)}"
        )
    }
}