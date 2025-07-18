package com.example.gymtracker.ui.stats

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.database.repository.Workout
import com.example.gymtracker.database.repository.WorkoutSession
import com.example.gymtracker.database.repository.WorkoutType
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
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
        workouts = uiState.workouts,
        workoutSessions = uiState.allWorkoutSessions,
        workoutSessionsBetweenDates = uiState.workoutSessionsBetweenDates
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    workoutSessionsBetweenDates: List<WorkoutSession>
) {
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
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            item {
                Calendar(
                    workouts = workouts,
                    workoutSessions = workoutSessionsBetweenDates
                )
            }
            item {
                PieChartCard(
                    workouts = workouts,
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

val highlightColors = listOf(
    Color(0xFFFF7D7D),
    Color(0xFFFFDF7D),
    Color(0xFFAAFF7D),
    Color(0xFF7DFFDA),
    Color(0xFF7D9EFF),
    Color(0xFFD17DFF),
    Color(0xFFFFFFFF),
)

@OptIn(ExperimentalTime::class)
@Composable
private fun Calendar(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
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
    val workoutsByName: Map<String, Workout> = remember(workouts) {
        workouts.associateBy { it.name }
    }

    StatsCard(modifier = modifier) {
        Text(
            text = currentMonth.month.getDisplayName(
                TextStyle.FULL_STANDALONE,
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
                        TextStyle.SHORT,
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
                        val workoutIndex = workouts.indexOf(workoutsByName[session.name])
                        WorkoutIcon(
                            painter =
                                if (session.type == WorkoutType.GYM) painterResource(id = R.drawable.weight)
                                else painterResource(id = R.drawable.run),
                            tint = if (workoutIndex < 0) Color.Transparent else highlightColors[workoutIndex]
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
            WorkoutFooter(
                workouts = workouts,
                workoutSessions = workoutSessions,
                workoutsByName = workoutsByName
            )
        }
    }
}

@Composable
fun WorkoutFooter(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    workoutsByName: Map<String, Workout>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier.fillMaxWidth()
    ) {
        workouts.forEach { workout ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoutIcon(
                    painter =
                        if (workout.type == WorkoutType.GYM) painterResource(id = R.drawable.weight)
                        else painterResource(id = R.drawable.run),
                    tint = highlightColors[workouts.indexOf(workoutsByName[workout.name])]
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

@Composable
private fun WorkoutIcon(
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

@Composable
private fun PieChartCard(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val sessionsByName = remember(workoutSessions) {
        workoutSessions.groupBy { it.name }
    }
    val workoutsByName: Map<String, Workout> = remember(workouts) {
        workouts.associateBy { it.name }
    }

    StatsCard(modifier = modifier) {
        var data by remember(sessionsByName) {
            mutableStateOf(
                sessionsByName.map { session ->
                    val workoutIndex = workouts.indexOf(workoutsByName[session.key])
                    val color =
                        if (workoutIndex < 0) Color.Transparent else highlightColors[workoutIndex]
                    Pie(
                        label = session.key,
                        data = session.value.size.toDouble(),
                        color = color,
                        selectedColor = color.copy(alpha = .5f)
                    )
                }
            )
        }
        PieChart(
            modifier = Modifier.size(200.dp),
            data = data,
            onPieClick = {
                val pieIndex = data.indexOf(it)
                data = data.mapIndexed { mapIndex, pie ->
                    pie.copy(selected = pieIndex == mapIndex)
                }
            },
            selectedScale = 1.2f,
            scaleAnimEnterSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            colorAnimEnterSpec = tween(300),
            colorAnimExitSpec = tween(300),
            scaleAnimExitSpec = tween(300),
            spaceDegreeAnimExitSpec = tween(300),
            style = Pie.Style.Fill
        )
    }
}

@Composable
private fun StatsScreenForPreview() {
    StatsOverviewScreen(
        loading = false,
        workouts = listOf(
            Workout(
                name = "My workout",
                type = WorkoutType.GYM
            ),
            Workout(
                name = "My cardio",
                type = WorkoutType.CARDIO
            ),
            Workout(
                name = "Super workout",
                type = WorkoutType.GYM
            ),
            Workout(
                name = "Super cardio",
                type = WorkoutType.CARDIO
            )
        ),
        workoutSessionsBetweenDates = listOf(
            WorkoutSession(
                name = "My workout",
                timestamp = Instant.now().minus(Duration.ofDays(1)),
                type = WorkoutType.GYM
            ),
            WorkoutSession(
                name = "My cardio",
                timestamp = Instant.now(),
                type = WorkoutType.CARDIO
            )
        ),
        workoutSessions = listOf(
            WorkoutSession(
                name = "My workout",
                timestamp = Instant.now().minus(Duration.ofDays(1)),
                type = WorkoutType.GYM
            ),
            WorkoutSession(
                name = "My cardio",
                timestamp = Instant.now(),
                type = WorkoutType.CARDIO
            ),
            WorkoutSession(
                name = "Super workout",
                timestamp = Instant.now().minus(Duration.ofDays(40)),
                type = WorkoutType.GYM
            ),
            WorkoutSession(
                name = "Super cardio",
                timestamp = Instant.now().minus(Duration.ofDays(50)),
                type = WorkoutType.CARDIO
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun StatsOverviewPreview() {
    GymTrackerTheme {
        StatsScreenForPreview()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatsOverviewPreviewDark() {
    GymTrackerTheme {
        StatsScreenForPreview()
    }
}
