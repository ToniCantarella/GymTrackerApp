package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOverviewScreen(
    viewModel: StatsOverviewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllStats()
    }

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.stats),
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (uiState.loading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                    contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_large)),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {
                        WorkoutCalendar(
                            sessionsForMonth = uiState.calendarSessions,
                            gymLegends = uiState.calendarGymLegends,
                            cardioLegends = uiState.calendarCardioLegends,
                            getMonthSessions = viewModel::getMonthData,
                            onGymSessionClick = viewModel::onGymSessionNavigate,
                            onCardioSessionClick = viewModel::onCardioSessionNavigate,
                            onAddGymSessionClick = viewModel::onAddGymSessionNavigate,
                            onAddCardioSessionClick = viewModel::onAddCardioSessionNavigate,
                            gymColorIndexMap = uiState.gymColorIndexMap,
                            cardioColorIndexMap = uiState.cardioColorIndexMap,
                            gymWorkouts = uiState.gymWorkouts,
                            cardioWorkouts = uiState.cardioWorkouts,
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                                .widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
                        )
                    }

                    item {
                        val itemWidth = 300.dp

                        Row(
                            modifier = Modifier
                                .widthIn(min = dimensionResource(id = R.dimen.breakpoint_small))
                                .padding(start = dimensionResource(id = R.dimen.padding_large))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.history),
                                contentDescription = stringResource(id = R.string.stats),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                            Text(
                                text = stringResource(id = R.string.all_time),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_large)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                            modifier = Modifier
                                .height(350.dp)
                        ) {
                            item {
                                WorkoutPieChart(
                                    legends = uiState.gymLegends,
                                    workoutType = WorkoutType.GYM,
                                    colorIndexMap = uiState.gymColorIndexMap,
                                    modifier = Modifier
                                        .width(itemWidth)
                                )
                            }
                            item {
                                WorkoutPieChart(
                                    legends = uiState.cardioLegends,
                                    workoutType = WorkoutType.CARDIO,
                                    colorIndexMap = uiState.cardioColorIndexMap,
                                    modifier = Modifier
                                        .width(itemWidth)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                        Column(
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                                .widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.timeline),
                                    contentDescription = stringResource(id = R.string.stats),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                                Text(
                                    text = stringResource(id = R.string.stats),
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))

                            if (uiState.gymWorkoutsGeneralStats.isNotEmpty() || uiState.cardioWorkoutsGeneralStats.isNotEmpty()) {
                                GeneralWorkoutStatsList(
                                    gymWorkouts = uiState.gymWorkoutsGeneralStats,
                                    cardioWorkouts = uiState.cardioWorkoutsGeneralStats,
                                    onGymWorkoutStatsNavigate = viewModel::onGymWorkoutStatsNavigate,
                                    onCardioWorkoutStatsNavigate = viewModel::onCardioWorkoutStatsNavigate,
                                    gymColorIndexMap = uiState.gymColorIndexMap,
                                    cardioColorIndexMap = uiState.cardioColorIndexMap
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}