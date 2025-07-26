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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.repository.Workout
import com.example.gymtracker.database.repository.WorkoutSession
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.MAX_CARDIO
import com.example.gymtracker.utility.MAX_SPLITS
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaYearMonth
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinYearMonth
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
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
        workoutSessionsBetweenDates = uiState.workoutSessionsBetweenDates,
        getMonthData = viewModel::getMonthData
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    workoutSessionsBetweenDates: List<WorkoutSession>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit
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
        val (gymWorkouts, cardioWorkouts) = remember(workouts) {
            workouts.partition { it.type == WorkoutType.GYM }
        }

        val (gymSessions, cardioSessions) = remember(workoutSessions) {
            workoutSessions.partition { it.workout.type == WorkoutType.GYM }
        }

        LazyColumn(
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            item {
                CalendarCard(
                    workouts = workouts,
                    workoutSessions = workoutSessionsBetweenDates,
                    getMonthData = getMonthData
                )
            }
            if (gymWorkouts.isNotEmpty()) {
                item {
                    PieChartCard(
                        workouts = gymWorkouts,
                        workoutSessions = gymSessions
                    )
                }
            }
            if (cardioWorkouts.isNotEmpty()) {
                item {
                    PieChartCard(
                        workouts = cardioWorkouts,
                        workoutSessions = cardioSessions
                    )
                }
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
    Color(0xFFFF0000),
    Color(0xFFFF7E00),
    Color(0xFF25FF00),
    Color(0xFF00E1FF),
    Color(0xFF0080FF),
    Color(0xFFD900FF),
    Color(0xFF5900FF)
)

@OptIn(ExperimentalTime::class)
@Composable
private fun CalendarCard(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    val state = rememberCalendarState(
        startMonth = startMonth.toKotlinYearMonth(),
        endMonth = endMonth.toKotlinYearMonth(),
        firstVisibleMonth = currentMonth.toKotlinYearMonth(),
        firstDayOfWeek = daysOfWeek.first()
    )

    val sessionsByDate: Map<LocalDate, List<WorkoutSession>> = remember(workoutSessions) {
        val zoneId = ZoneId.systemDefault()
        workoutSessions.groupBy {
            it.timestamp.atZone(zoneId).toLocalDate().toKotlinLocalDate()
        }
    }
    val (gymWorkouts, cardioWorkouts) = remember(workouts) {
        workouts.partition { it.type == WorkoutType.GYM }
    }
    val gymWorkoutsById = remember(gymWorkouts) { gymWorkouts.associateBy { it.id } }
    val cardioWorkoutsById = remember(cardioWorkouts) { cardioWorkouts.associateBy { it.id } }

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth.yearMonth }
            .distinctUntilChanged()
            .collect { visibleMonth ->
                val zoneId = ZoneId.systemDefault()

                val startDate: Instant = visibleMonth.toJavaYearMonth()
                    .atDay(1)
                    .atStartOfDay(zoneId)
                    .toInstant()

                val endDate: Instant = visibleMonth.toJavaYearMonth()
                    .atEndOfMonth()
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .minus(Duration.ofMillis(1))

                getMonthData(startDate, endDate)
            }
    }

    StatsCard(modifier = modifier) {
        Text(
            text = state.firstVisibleMonth.yearMonth.toJavaYearMonth().month.getDisplayName(
                TextStyle.FULL_STANDALONE,
                Locale.getDefault()
            ),
            modifier = Modifier.padding(
                bottom = dimensionResource(id = R.dimen.padding_medium)
            )
        )
        HorizontalCalendar(
            state = state,
            monthHeader = {
                Row(
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                        .fillMaxWidth()
                ) {
                    daysOfWeek.forEach { dayOfWeek ->
                        Text(
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            text = dayOfWeek.toJavaDayOfWeek()
                                .getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.getDefault()
                                )
                        )
                    }
                }
            },
            dayContent = { day ->
                val sessionsOnDay = sessionsByDate[day.date]

                Day(day = day) {
                    if (sessionsOnDay?.isNotEmpty() == true) {
                        sessionsOnDay.forEach { session ->
                            if (session.workout.type == WorkoutType.GYM) {
                                val workoutIndex =
                                    gymWorkouts.indexOf(gymWorkoutsById[session.workout.id])
                                WorkoutIcon(
                                    painter = painterResource(id = R.drawable.weight),
                                    tint = highlightColors[workoutIndex % highlightColors.size]
                                )
                            } else {
                                val workoutIndex =
                                    cardioWorkouts.indexOf(cardioWorkoutsById[session.workout.id])
                                WorkoutIcon(
                                    painter = painterResource(id = R.drawable.run),
                                    tint = highlightColors[workoutIndex % highlightColors.size]
                                )
                            }
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        CalendarFooter(
            workouts = workouts,
            workoutSessions = workoutSessions
        )
    }
}

@Composable
fun CalendarFooter(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val (gymWorkouts, cardioWorkouts) = remember(workouts) {
        workouts.partition { it.type == WorkoutType.GYM }
    }

    val (gymSessions, cardioSessions) = remember(workoutSessions) {
        workoutSessions.partition { it.workout.type == WorkoutType.GYM }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        WorkoutLegendsRow(
            workouts = gymWorkouts,
            workoutSessions = gymSessions
        )
        if (gymWorkouts.isNotEmpty() && cardioWorkouts.isNotEmpty()) {
            HorizontalDivider()
        }
        WorkoutLegendsRow(
            workouts = cardioWorkouts,
            workoutSessions = cardioSessions
        )
        Text(
            text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
        )
    }
}

@Composable
fun WorkoutLegendsRow(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val sessionsById = remember(workoutSessions) {
        workoutSessions.groupBy { it.workout.id }
    }

    FlowRow(
        maxItemsInEachRow = 3,
        modifier = modifier.fillMaxWidth()
    ) {
        workouts.forEachIndexed { index, workout ->
            val sessionsAmount = sessionsById[workout.id]?.size ?: 0

            WorkoutLegend(
                workout = workout,
                sessionsAmount = sessionsAmount,
                highlightColor = highlightColors[index % highlightColors.size],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WorkoutLegendsColumn(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val sessionsById = remember(workoutSessions) {
        workoutSessions.groupBy { it.workout.id }
    }

    Column(
        modifier = modifier
    ) {
        workouts.forEachIndexed { index, workout ->
            val sessionsAmount = sessionsById[workout.id]?.size ?: 0

            WorkoutLegend(
                workout = workout,
                sessionsAmount = sessionsAmount,
                highlightColor = highlightColors[index % highlightColors.size],
            )
        }
    }
}

@Composable
fun WorkoutLegend(
    workout: Workout,
    sessionsAmount: Int,
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
                if (workout.type == WorkoutType.GYM) painterResource(id = R.drawable.weight)
                else painterResource(id = R.drawable.run),
            tint = highlightColor
        )
        Text(
            text = "$sessionsAmount x",
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
        contentDescription = null,
        modifier = Modifier.size(16.dp)
    )
}

@OptIn(ExperimentalTime::class)
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
            text = day.date.day.toString(),
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
        workoutSessions.groupBy { it.workout.name }
    }
    val workoutsById: Map<Int, Workout> = remember(workouts) {
        workouts.associateBy { it.id }
    }
    var data by remember(sessionsByName) {
        mutableStateOf(
            sessionsByName.map { session ->
                val workoutIndex =
                    workouts.indexOf(workoutsById[session.value.first().workout.id])
                val color =
                    if (workoutIndex < 0) Color.Transparent else highlightColors[workoutIndex % highlightColors.size]
                Pie(
                    label = session.key,
                    data = session.value.size.toDouble(),
                    color = color,
                    selectedColor = color.copy(alpha = .5f)
                )
            }
        )
    }

    StatsCard(modifier = modifier) {
        Row(
            modifier = Modifier.height(180.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        Icon(
                            painter = if (workouts.first().type == WorkoutType.GYM) painterResource(
                                id = R.drawable.weight
                            ) else painterResource(id = R.drawable.run),
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(id = R.string.all_time)
                        )
                    }
                    WorkoutLegendsColumn(
                        workouts = workouts,
                        workoutSessions = workoutSessions
                    )
                }
                Text(
                    text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
                )
            }
            PieChart(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                data = data,
                onPieClick = {
                    val clickedIndex = data.indexOf(it)
                    val isAlreadySelected = data.getOrNull(clickedIndex)?.selected == true

                    data = data.mapIndexed { index, pie ->
                        pie.copy(selected = if (clickedIndex == index) !isAlreadySelected else false)
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
}

@Composable
private fun StatsScreenForPreview() {
    val workouts = List(MAX_SPLITS + MAX_CARDIO) {
        val name =
            if (it == 0)
                "This is a very long name that can overflow"
            else
                "workout ${it + 1}"
        Workout(
            id = it,
            name = name,
            type = if (it < MAX_SPLITS) WorkoutType.GYM else WorkoutType.CARDIO
        )
    }

    val now = Instant.now()

    val workoutsBetweenDates = List(20) {
        val timestamp = if (it > 4) now.minus(Duration.ofDays(it.toLong())) else now
        WorkoutSession(
            workout = workouts.random(),
            timestamp = timestamp
        )
    }

    val workoutsAllTime = List(10) {
        WorkoutSession(
            workout = workouts.random(),
            timestamp = now.minus(Duration.ofDays(it.toLong()))
        )
    } + workoutsBetweenDates

    StatsOverviewScreen(
        loading = false,
        workouts = workouts,
        workoutSessionsBetweenDates = workoutsBetweenDates,
        workoutSessions = workoutsAllTime,
        getMonthData = { _, _ -> }
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
