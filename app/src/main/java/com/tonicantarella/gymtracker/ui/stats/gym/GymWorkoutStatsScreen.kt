package com.tonicantarella.gymtracker.ui.stats.gym

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.stats.BasicLineChart
import com.tonicantarella.gymtracker.utility.UnitUtil
import com.tonicantarella.gymtracker.utility.toDateString
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymWorkoutStatsScreen(
    viewModel: GymWorkoutStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.stats?.name ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (uiState.loading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.stats != null) {
                GymWorkoutStatsScreen(
                    stats = uiState.stats!!
                )
            }
        }
    }
}

@Composable
private fun GymWorkoutStatsScreen(
    stats: GymWorkoutStats,
    modifier: Modifier = Modifier
) {
    val weightUnitString = stringResource(id = UnitUtil.weightUnitStringId)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        itemsIndexed(stats.exercises) { index, exercise ->
            HorizontalDivider()
            BasicLineChart(
                title = {
                    Text(
                        text = exercise.name.ifEmpty { "${stringResource(id = R.string.exercise)} ${index + 1}" }
                    )
                },
                bottomLabels = if (exercise.setHistory.isNotEmpty()) {
                    listOf(
                        exercise.setHistory.first().timestamp.toDateString(),
                        exercise.setHistory.last().timestamp.toDateString()
                    )
                } else emptyList(),
                dataValues = exercise.setHistory.map { it.maxWeight },
                popupContentBuilder = { dataIndex, valueIndex, value ->
                    "${exercise.setHistory[valueIndex].maxWeight} ${weightUnitString}\n ${exercise.setHistory[valueIndex].timestamp.toDateString()}"
                }
            )
        }
    }
}