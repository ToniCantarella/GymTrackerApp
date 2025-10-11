package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.stats.BasicBarChart
import com.tonicantarella.gymtracker.ui.stats.BasicLineChart
import com.tonicantarella.gymtracker.ui.stats.common.GymGeneralStats
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.toDateString

enum class StatsTabs {
    WEIGHT,
    REPETITIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymWorkoutStatsList(
    stats: GymWorkoutStats,
    modifier: Modifier = Modifier,
    generalStats: GymWorkoutWithGeneralStats? = null
) {
    var selectedTab by remember { mutableStateOf(StatsTabs.WEIGHT) }
    val weightUnitString = stringResource(id = UnitUtil.weightUnitStringId)

    Column(modifier = modifier) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = selectedTab == StatsTabs.WEIGHT,
                onClick = { selectedTab = StatsTabs.WEIGHT },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.dumbbell),
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.weight)
                    )
                }
            )
            Tab(
                selected = selectedTab == StatsTabs.REPETITIONS,
                onClick = { selectedTab = StatsTabs.REPETITIONS },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.stats),
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.repetitions)
                    )
                }
            )
        }
        LazyColumn {
            if (generalStats != null) {
                item {
                    GymGeneralStats(
                        stats = generalStats,
                        modifier = Modifier.padding(
                            dimensionResource(id = R.dimen.padding_large)
                        )
                    )
                    HorizontalDivider()
                }
            }
            when (selectedTab) {
                StatsTabs.WEIGHT ->
                    itemsIndexed(stats.exercises) { index, exercise ->
                        BasicLineChart(
                            title = {
                                Text(
                                    text = exercise.name.ifEmpty {
                                        "${stringResource(id = R.string.exercise)} ${index + 1}"
                                    }
                                )
                            },
                            bottomLabels =
                                if (exercise.setHistory.isNotEmpty()) {
                                    listOf(
                                        exercise.setHistory.first().timestamp.toDateString(),
                                        exercise.setHistory.last().timestamp.toDateString()
                                    )
                                } else
                                    emptyList(),
                            //TODO add all setdata here and draw multiple lines
                            dataValues = exercise.setHistory.map { it.maxWeight },
                            popupContentBuilder = { dataIndex, valueIndex, value ->
                                "${exercise.setHistory[valueIndex].maxWeight} ${weightUnitString}\n ${exercise.setHistory[valueIndex].timestamp.toDateString()}"
                            }
                        )
                        HorizontalDivider()
                    }


                StatsTabs.REPETITIONS ->
                    itemsIndexed(stats.exercises) { index, exercise ->
                        BasicBarChart(
                            title = {
                                Text(
                                    text = exercise.name.ifEmpty {
                                        "${stringResource(id = R.string.exercise)} ${index + 1}"
                                    }
                                )
                            },
                            bottomLabels =
                                if (exercise.setHistory.isNotEmpty()) {
                                    listOf(
                                        exercise.setHistory.first().timestamp.toDateString(),
                                        exercise.setHistory.last().timestamp.toDateString()
                                    )
                                } else
                                    emptyList(),
                            dataValues = exercise.setHistory,
                            popupContentBuilder = { dataIndex, valueIndex, value ->
                                "${value.toInt()}\n ${exercise.setHistory[valueIndex].timestamp.toDateString()}"
                            }
                        )
                        HorizontalDivider()
                    }

            }
        }
    }
}