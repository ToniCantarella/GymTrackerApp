package com.tonicantarella.gymtracker.ui.stats.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.statsoverview.CardioWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.toReadableString

@Composable
fun CardioGeneralStats(
    stats: CardioWorkoutWithGeneralStats,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_steps)}:",
            value = "${stats.avgSteps}"
        )
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_distance)}:",
            value = "${stats.avgDistance} ${stringResource(UnitUtil.distanceUnitStringId)}"
        )
        LabelValueRow(
            label = "${stringResource(id = R.string.avg_duration)}:",
            value = stats.avgDuration.toReadableString()
        )
    }
}