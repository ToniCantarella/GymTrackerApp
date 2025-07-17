package com.example.gymtracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.WorkoutSession
import com.example.gymtracker.database.repository.WorkoutType
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId
import java.util.Locale
import kotlin.time.ExperimentalTime

@Composable
fun StatsOverviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsOverviewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.overview)
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        }
    )

    StatsOverviewScreen(
        loading = uiState.loading,
        workoutSessions = uiState.workoutSessions
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    workoutSessions: List<WorkoutSession>
) {
    if (loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            item {
                Calendar(
                    workoutSessions = workoutSessions
                )
            }

        }
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                dimensionResource(id = R.dimen.padding_large)
            )
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun Calendar(
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val highlightColors = listOf(
        Color(0xFFFF7D7D),
        Color(0xFFFFDF7D),
        Color(0xFFAAFF7D),
        Color(0xFF7DFFDA),
        Color(0xFF7D9EFF),
        Color(0xFFD17DFF),
    )

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val sessionsByDate: Map<LocalDate, List<WorkoutSession>> = remember(workoutSessions) {
        val zoneId = ZoneId.systemDefault()
        workoutSessions.groupBy {
            it.timestamp.atZone(zoneId).toLocalDate().toKotlinLocalDate()
        }
    }
    val workouts = remember(workoutSessions) {
        workoutSessions.groupBy { it.name }
    }
    val workoutColors: Map<String, Color> = remember(workouts) {
        workouts.keys.mapIndexed { index, name ->
            name to highlightColors[index % highlightColors.size]
        }.toMap()
    }

    StatsCard(modifier = modifier) {
        Text(
            text = currentMonth.month.getDisplayName(
                java.time.format.TextStyle.FULL_STANDALONE,
                Locale.getDefault()
            ),
            modifier = Modifier.padding(
                bottom = dimensionResource(id = R.dimen.padding_medium)
            )
        )
        Row(
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                .fillMaxWidth()
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.getDefault()
                    ),
                )
            }
        }
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val sessionsOnDay = sessionsByDate[day.date].orEmpty()

                Day(
                    day = day,
                ) {
                    sessionsOnDay.forEach { session ->
                        CalendarIcon(
                            painter =
                                if (session.type == WorkoutType.GYM) painterResource(id = R.drawable.weight)
                                else painterResource(id = R.drawable.run),
                            tint = workoutColors[session.name]
                        )
                    }
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small))
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                modifier = Modifier.fillMaxWidth()
            ) {
                workouts.forEach {
                    val workout = it.value.first()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarIcon(
                            painter =
                                if (workout.type == WorkoutType.GYM) painterResource(id = R.drawable.weight)
                                else painterResource(id = R.drawable.run),
                            tint = workoutColors[workout.name]
                        )
                        Text(
                            text = workout.name
                        )
                    }
                }
            }
            Text(
                text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
            )
        }
    }
}

@Composable
private fun CalendarIcon(
    painter: Painter,
    tint: Color? = null
) {
    Icon(
        painter = painter,
        tint = tint ?: MaterialTheme.colorScheme.onSurface,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
    )
}

val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

@Composable
private fun Day(
    day: CalendarDay,
    contentIcons: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .then(
                if (day.date == today)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            contentIcons()
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate && day.date <= today)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(.5f),
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

/*
@Preview(showBackground = true)
@Composable
private fun StatsOverviewPreview() {
    GymTrackerTheme {
        StatsOverviewScreen(
            loading = false,
            workoutDays = emptyList()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatsOverviewPreviewDark() {
    GymTrackerTheme {
        StatsOverviewScreen(
            loading = false,
            workoutDays = emptyList()
        )
    }
}*/
