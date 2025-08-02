package com.example.gymtracker.ui.stats.overview

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.repository.Workout
import com.example.gymtracker.database.repository.WorkoutSession
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.MAX_CARDIO
import com.example.gymtracker.utility.MAX_SPLITS
import com.example.gymtracker.utility.toDateAndTimeString
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaLocalDate
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
    onSessionNavigate: (id: Int, type: WorkoutType) -> Unit,
    onWorkoutNavigate: (workout: Workout) -> Unit,
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
        gymWorkouts = uiState.gymWorkouts,
        cardioWorkouts = uiState.cardioWorkouts,
        gymSessions = uiState.gymSessions,
        cardioSessions = uiState.cardioSessions,
        workoutSessionsForMonth = uiState.workoutSessionsBetween,
        getMonthData = viewModel::getMonthData,
        onSessionClick = onSessionNavigate,
        onWorkoutNavigate = onWorkoutNavigate
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    gymWorkouts: List<Workout>,
    cardioWorkouts: List<Workout>,
    gymSessions: List<WorkoutSession>,
    cardioSessions: List<WorkoutSession>,
    workoutSessionsForMonth: List<WorkoutSession>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onSessionClick: (id: Int, type: WorkoutType) -> Unit,
    onWorkoutNavigate: (workout: Workout) -> Unit
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
                CalendarCard(
                    gymWorkouts = gymWorkouts,
                    cardioWorkouts = cardioWorkouts,
                    sessionsForMonth = workoutSessionsForMonth,
                    getMonthData = getMonthData,
                    onSessionClick = onSessionClick
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
            if (gymWorkouts.isNotEmpty() || cardioWorkouts.isNotEmpty()) {
                item {
                    WorkoutListing(
                        gymWorkouts = gymWorkouts,
                        cardioWorkouts = cardioWorkouts,
                        onWorkoutNavigate = onWorkoutNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutListing(
    gymWorkouts: List<Workout>,
    cardioWorkouts: List<Workout>,
    onWorkoutNavigate: (workout: Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier.fillMaxWidth()
    ) {
        if (gymWorkouts.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.weight),
                    contentDescription = stringResource(id = R.string.gym)
                )
                gymWorkouts.forEach {
                    WorkoutCard(
                        workout = it,
                        onClick = { onWorkoutNavigate(it) }
                    )
                }
            }
        }
        if (cardioWorkouts.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.run),
                    contentDescription = stringResource(id = R.string.cardio)
                )
                cardioWorkouts.forEach {
                    WorkoutCard(
                        workout = it,
                        onClick = { onWorkoutNavigate(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_medium))
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.timeline),
                contentDescription = stringResource(id = R.string.stats)
            )
            Text(
                text = workout.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
    Color(0xFFE63946),
    Color(0xFFF1A208),
    Color(0xFF04DEA5),
    Color(0xFF3A86FF),
    Color(0xFF8338EC),
    Color(0xFFB5179E),
    Color(0xFF16A144)
)

@OptIn(ExperimentalTime::class)
@Composable
private fun CalendarCard(
    gymWorkouts: List<Workout>,
    cardioWorkouts: List<Workout>,
    sessionsForMonth: List<WorkoutSession>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onSessionClick: (id: Int, type: WorkoutType) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val zoneId = ZoneId.systemDefault()

    val currentMonth = remember { YearMonth.now().toKotlinYearMonth() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    var sessionDialogOpen by remember { mutableStateOf(false) }
    val todayButtonVisible =
        currentMonth != state.firstVisibleMonth.yearMonth

    val allWorkouts = gymWorkouts + cardioWorkouts
    var sessionsForDialog: List<WorkoutSession> by remember { mutableStateOf(emptyList()) }
    val sessionsByDate: Map<LocalDate, List<WorkoutSession>> = remember(sessionsForMonth) {
        sessionsForMonth.groupBy {
            it.timestamp.atZone(zoneId).toLocalDate().toKotlinLocalDate()
        }
    }
    val sessionsForMonth = remember(sessionsForMonth, state) {
        sessionsForMonth.filter {
            YearMonth.from(it.timestamp.atZone(zoneId))
                .toKotlinYearMonth() == state.firstVisibleMonth.yearMonth
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth }
            .distinctUntilChanged()
            .collect { visibleMonth ->
                val firstVisibleDate = visibleMonth.weekDays.first().first().date.toJavaLocalDate()
                val lastVisibleDate = visibleMonth.weekDays.last().last().date.toJavaLocalDate()

                val startDate: Instant = firstVisibleDate
                    .atStartOfDay(zoneId)
                    .toInstant()

                val endDate: Instant = lastVisibleDate
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .minus(Duration.ofMillis(1))

                getMonthData(startDate, endDate)
            }
    }

    StatsCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val monthString =
                state.firstVisibleMonth.yearMonth.toJavaYearMonth().month.getDisplayName(
                    TextStyle.FULL_STANDALONE,
                    Locale.getDefault()
                )
            val yearString = state.firstVisibleMonth.yearMonth.year
            Text(
                text = "$monthString $yearString",
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
            )
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(
                            currentMonth
                        )
                    }
                },
                enabled = todayButtonVisible
            ) {
                AnimatedVisibility(
                    visible = todayButtonVisible
                ) {

                    Text(
                        text = stringResource(id = R.string.today)
                    )
                }
            }
        }
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
                val hasSessions = sessionsOnDay?.isNotEmpty() == true

                fun onClick() {
                    if (sessionsOnDay?.size!! == 1) {
                        val session = sessionsOnDay.first()
                        val workout = allWorkouts.find { it.id == session.workoutId }
                        onSessionClick(session.id, workout!!.type)
                    } else {
                        sessionDialogOpen = true
                        sessionsForDialog = sessionsOnDay
                    }
                }

                Day(
                    day = day,
                    onClick = if (hasSessions) ::onClick else null
                ) {
                    if (hasSessions) {
                        sessionsOnDay.forEach { session ->
                            val workout = allWorkouts.find { it.id == session.workoutId }

                            if (workout?.type == WorkoutType.GYM) {
                                val workoutIndex =
                                    gymWorkouts.indexOf(workout)
                                WorkoutIcon(
                                    painter = painterResource(id = R.drawable.weight),
                                    tint = highlightColors[workoutIndex % highlightColors.size]
                                )
                            } else {
                                val workoutIndex =
                                    cardioWorkouts.indexOf(workout)
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
            gymWorkouts = gymWorkouts,
            cardioWorkouts = cardioWorkouts,
            workoutSessions = sessionsForMonth
        )
    }

    if (sessionDialogOpen) {
        Dialog(
            onDismissRequest = { sessionDialogOpen = false }
        ) {
            ElevatedCard {
                LazyColumn(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_large))
                        .fillMaxHeight(.5f)
                ) {
                    itemsIndexed(sessionsForDialog) { index, session ->
                        val workout = allWorkouts.find { it.id == session.workoutId }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSessionClick(session.id, workout!!.type)
                                }
                        ) {
                            Icon(
                                painter = if (workout!!.type == WorkoutType.GYM)
                                    painterResource(id = R.drawable.weight)
                                else
                                    painterResource(id = R.drawable.run),
                                contentDescription = stringResource(id = R.string.icon)
                            )
                            Column(
                                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                            ) {
                                Text(
                                    text = workout.name
                                )
                                Text(
                                    text = session.timestamp.toDateAndTimeString()
                                )
                            }
                        }
                        if (sessionsForDialog.size > 1 && index != sessionsForDialog.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarFooter(
    gymWorkouts: List<Workout>,
    cardioWorkouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val sessionsByWorkoutId = remember(workoutSessions) {
        workoutSessions.groupBy { it.workoutId }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        WorkoutLegendsRow(
            workouts = gymWorkouts,
            sessionsByWorkoutId = sessionsByWorkoutId
        )
        if (gymWorkouts.isNotEmpty() && cardioWorkouts.isNotEmpty()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )
        }
        WorkoutLegendsRow(
            workouts = cardioWorkouts,
            sessionsByWorkoutId = sessionsByWorkoutId
        )
        Text(
            text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
        )
    }
}

@Composable
fun WorkoutLegendsRow(
    workouts: List<Workout>,
    sessionsByWorkoutId: Map<Int, List<WorkoutSession>>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        maxItemsInEachRow = 3,
        modifier = modifier.fillMaxWidth()
    ) {
        workouts.forEachIndexed { index, workout ->
            val sessionsAmount = sessionsByWorkoutId[workout.id]?.size ?: 0

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
    sessionsByWorkoutId: Map<Int, List<WorkoutSession>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        workouts.forEachIndexed { index, workout ->
            val sessionsAmount = sessionsByWorkoutId[workout.id]?.size ?: 0

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
        contentDescription = stringResource(id = R.string.icon),
        modifier = Modifier.size(16.dp)
    )
}

@OptIn(ExperimentalTime::class)
val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

@Composable
private fun Day(
    day: CalendarDay,
    onClick: (() -> Unit)? = null,
    contentIcons: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .alpha(if (day.position == DayPosition.MonthDate && day.date <= today) 1f else .4f)
            .clickable(
                enabled = onClick != null
            ) {
                onClick?.invoke()
            }
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
            color = MaterialTheme.colorScheme.onSurface,
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
    val sessionsByWorkoutId = remember(workoutSessions) {
        workoutSessions.groupBy { it.workoutId }
    }

    var data by remember {
        mutableStateOf(
            workouts.map { workout ->
                val workoutIndex = workouts.indexOf(workout)
                val amountOfSessions = sessionsByWorkoutId[workout.id]?.size?.toDouble() ?: 0.0
                val color = highlightColors[workoutIndex % highlightColors.size]
                Pie(
                    label = workout.name,
                    data = amountOfSessions,
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
                            contentDescription = stringResource(id = R.string.icon)
                        )
                        Text(
                            text = stringResource(id = R.string.all_time)
                        )
                    }
                    WorkoutLegendsColumn(
                        workouts = workouts,
                        sessionsByWorkoutId = sessionsByWorkoutId
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
    val gymWorkouts = List(MAX_SPLITS) {
        val name =
            if (it == 0)
                "This is a very long name that can overflow"
            else
                "Gym ${it + 1}"
        Workout(
            id = it,
            name = name,
            type = WorkoutType.GYM
        )
    }
    val cardioWorkouts = List(MAX_CARDIO) {
        val name =
            if (it == 0)
                "This is a very long name that can overflow"
            else
                "Cardio ${it + 1}"
        Workout(
            id = it + MAX_SPLITS,
            name = name,
            type = WorkoutType.CARDIO
        )
    }
    val allWorkouts = gymWorkouts + cardioWorkouts

    val now = Instant.now()

    val workoutSessionsBetween = List(20) {
        val timestamp = if (it > 4) now.minus(Duration.ofDays(it.toLong())) else now
        WorkoutSession(
            id = 0,
            workoutId = allWorkouts.random().id,
            timestamp = timestamp
        )
    }

    val gymSessions = List(20) {
        val timestamp = if (it > 4) now.minus(Duration.ofDays(it.toLong())) else now
        WorkoutSession(
            id = 0,
            workoutId = gymWorkouts.random().id,
            timestamp = timestamp
        )
    }
    val cardioSessions = List(20) {
        val timestamp = if (it > 4) now.minus(Duration.ofDays(it.toLong())) else now
        WorkoutSession(
            id = 0,
            workoutId = cardioWorkouts.random().id,
            timestamp = timestamp
        )
    }

    StatsOverviewScreen(
        loading = false,
        gymWorkouts = gymWorkouts,
        cardioWorkouts = cardioWorkouts,
        gymSessions = gymSessions,
        cardioSessions = cardioSessions,
        workoutSessionsForMonth = workoutSessionsBetween,
        getMonthData = { _, _ -> },
        onSessionClick = { _, _ -> },
        onWorkoutNavigate = {}
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
