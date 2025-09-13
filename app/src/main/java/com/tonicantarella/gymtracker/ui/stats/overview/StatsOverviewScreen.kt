package com.tonicantarella.gymtracker.ui.stats.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutLegend
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOverviewScreen(
    onNavigateBack: () -> Unit,
    onGymSessionNavigate: (id: Int) -> Unit,
    onCardioSessionNavigate: (id: Int) -> Unit,
    onAddGymSessionNavigate: (workoutId: Int, timestamp: Instant) -> Unit,
    onAddCardioSessionNavigate: (workoutId: Int, timestamp: Instant) -> Unit,
    onGymWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    onCardioWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    viewModel: StatsOverviewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.stats),
                    )
                }
            )
        }
    ) {innerPadding ->
        StatsOverviewScreen(
            loading = uiState.loading,
            gymWorkouts = uiState.gymWorkouts,
            cardioWorkouts = uiState.cardioWorkouts,
            gymLegends = uiState.gymLegends,
            cardioLegends = uiState.cardioLegends,
            calendarSessions = uiState.calendarSessions,
            calendarGymLegends = uiState.calendarGymLegends,
            calendarCardioLegends = uiState.calendarCardioLegends,
            getMonthData = viewModel::getMonthData,
            onGymSessionNavigate = onGymSessionNavigate,
            onCardioSessionNavigate = onCardioSessionNavigate,
            onAddGymSessionNavigate = onAddGymSessionNavigate,
            onAddCardioSessionNavigate = onAddCardioSessionNavigate,
            onGymWorkoutStatsNavigate = onGymWorkoutStatsNavigate,
            onCardioWorkoutStatsNavigate = onCardioWorkoutStatsNavigate,
            gymColorIndexMap = uiState.gymColorIndexMap,
            cardioColorIndexMap = uiState.cardioColorIndexMap,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
    gymLegends: List<WorkoutLegend>,
    cardioLegends: List<WorkoutLegend>,
    calendarSessions: Map<LocalDate, List<WorkoutSession>>,
    calendarGymLegends: List<WorkoutLegend>,
    calendarCardioLegends: List<WorkoutLegend>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onGymSessionNavigate: (id: Int) -> Unit,
    onCardioSessionNavigate: (id: Int) -> Unit,
    onAddGymSessionNavigate: (workoutId: Int, timestamp: Instant) -> Unit,
    onAddCardioSessionNavigate: (workoutId: Int, timestamp: Instant) -> Unit,
    onGymWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    onCardioWorkoutStatsNavigate: (workoutId: Int) -> Unit,
    gymColorIndexMap: Map<Int, Int>,
    cardioColorIndexMap: Map<Int, Int>,
    modifier : Modifier = Modifier
) {
    Box(modifier = modifier){
        if (loading) {
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
                contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
            ) {
                item {
                    WorkoutCalendar(
                        sessionsForMonth = calendarSessions,
                        gymLegends = calendarGymLegends,
                        cardioLegends = calendarCardioLegends,
                        getMonthSessions = getMonthData,
                        onGymSessionClick = onGymSessionNavigate,
                        onCardioSessionClick = onCardioSessionNavigate,
                        onAddGymSessionClick = onAddGymSessionNavigate,
                        onAddCardioSessionClick = onAddCardioSessionNavigate,
                        gymColorIndexMap = gymColorIndexMap,
                        cardioColorIndexMap = cardioColorIndexMap,
                        gymWorkouts = gymWorkouts,
                        cardioWorkouts = cardioWorkouts,
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                    )
                }

                item {
                    val itemWidth = 300.dp

                    Text(
                        text = stringResource(id = R.string.all_time),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_large))
                    )
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_large)),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        item {
                            WorkoutPieChart(
                                legends = gymLegends,
                                workoutType = WorkoutType.GYM,
                                colorIndexMap = gymColorIndexMap,
                                modifier = Modifier
                                    .width(itemWidth)
                            )
                        }
                        item {
                            WorkoutPieChart(
                                legends = cardioLegends,
                                workoutType = WorkoutType.CARDIO,
                                colorIndexMap = cardioColorIndexMap,
                                modifier = Modifier
                                    .width(itemWidth)
                            )
                        }
                    }
                }


                item {
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                    Row(
                        modifier = Modifier
                            .padding(start = dimensionResource(id = R.dimen.padding_large))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timeline),
                            contentDescription = stringResource(id = R.string.stats),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = stringResource(id = R.string.stats),
                            style = MaterialTheme.typography.headlineLarge,

                            )
                    }
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))

                    if (gymWorkouts.isNotEmpty() || cardioWorkouts.isNotEmpty()) {
                        WorkoutListing(
                            gymWorkouts = gymWorkouts,
                            cardioWorkouts = cardioWorkouts,
                            onGymWorkoutStatsNavigate = onGymWorkoutStatsNavigate,
                            onCardioWorkoutStatsNavigate = onCardioWorkoutStatsNavigate,
                            gymColorIndexMap = gymColorIndexMap,
                            cardioColorIndexMap = cardioColorIndexMap
                        )
                    }
                }
            }
        }
    }
}