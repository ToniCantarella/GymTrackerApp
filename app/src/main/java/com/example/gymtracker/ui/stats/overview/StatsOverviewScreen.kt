package com.example.gymtracker.ui.stats.overview

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.repository.Workout
import com.example.gymtracker.database.repository.WorkoutSession
import com.example.gymtracker.database.repository.WorkoutWithLatestTimestamp
import com.example.gymtracker.ui.common.WorkoutListItem
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.MAX_SPLITS
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaYearMonth
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinMonth
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toKotlinYearMonth
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

@Composable
fun StatsOverviewScreen(
    onNavigateBack: () -> Unit,
    onSessionNavigate: (id: Int, type: WorkoutType) -> Unit,
    onAddSessionNavigate: (workout: Workout, timestamp: Instant) -> Unit,
    onWorkoutStatsNavigate: (workout: Workout) -> Unit,
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
        onSessionNavigate = onSessionNavigate,
        onAddSessionNavigate = onAddSessionNavigate,
        onWorkoutStatsNavigate = onWorkoutStatsNavigate
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
    onSessionNavigate: (id: Int, type: WorkoutType) -> Unit,
    onAddSessionNavigate: (workout: Workout, timestamp: Instant) -> Unit,
    onWorkoutStatsNavigate: (workout: Workout) -> Unit
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
            contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            item {
                StatCalendar(
                    gymWorkouts = gymWorkouts,
                    cardioWorkouts = cardioWorkouts,
                    sessionsForMonth = workoutSessionsForMonth,
                    getMonthData = getMonthData,
                    onSessionClick = onSessionNavigate,
                    onAddSessionClick = onAddSessionNavigate,
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                )
            }


            item {
                val itemWidth = 300.dp
                val itemHeight = 350.dp

                Text(
                    text = stringResource(id = R.string.all_time),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_large))
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_large)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        PieChartCard(
                            workouts = gymWorkouts.ifEmpty { emptyList() },
                            workoutSessions = gymSessions.ifEmpty { emptyList() },
                            type = WorkoutType.GYM,
                            modifier = Modifier
                                .width(itemWidth)
                                .height(itemHeight)
                        )
                    }
                    item {
                        PieChartCard(
                            workouts = cardioWorkouts.ifEmpty { emptyList() },
                            workoutSessions = cardioSessions.ifEmpty { emptyList() },
                            type = WorkoutType.CARDIO,
                            modifier = Modifier
                                .width(itemWidth)
                                .height(itemHeight)
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
                        onWorkoutNavigate = onWorkoutStatsNavigate
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
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
    ) {
        if (gymWorkouts.isNotEmpty()) {
            gymWorkouts.forEachIndexed { index, workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { onWorkoutNavigate(workout) },
                    iconColor = highlightColors[index % highlightColors.size]
                )
            }
        }
        if (cardioWorkouts.isNotEmpty()) {
            cardioWorkouts.forEachIndexed { index, workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { onWorkoutNavigate(workout) },
                    iconColor = highlightColors[index % highlightColors.size]
                )
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.padding_medium)
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter =
                        if (workout.type == WorkoutType.GYM)
                            painterResource(id = R.drawable.weight)
                        else
                            painterResource(id = R.drawable.run),
                    tint = iconColor,
                    contentDescription = stringResource(id = R.string.icon)
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = workout.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.keyboard_arrow_right),
                contentDescription = stringResource(R.string.select),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
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
private fun StatCalendar(
    gymWorkouts: List<Workout>,
    cardioWorkouts: List<Workout>,
    sessionsForMonth: List<WorkoutSession>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onSessionClick: (id: Int, type: WorkoutType) -> Unit,
    onAddSessionClick: (workout: Workout, timestamp: Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val zoneId = ZoneId.systemDefault()

    var addingSessions by remember { mutableStateOf(false) }
    var addSessionDialogOpen by remember { mutableStateOf(false) }
    var timestamp by remember { mutableStateOf<Instant?>(null) }

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

    val monthString =
        state.firstVisibleMonth.yearMonth.toJavaYearMonth().month.getDisplayName(
            TextStyle.FULL_STANDALONE,
            Locale.getDefault()
        )
    val yearString = state.firstVisibleMonth.yearMonth.year

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
            val sessionDate = it.timestamp.atZone(zoneId).toLocalDate()
            val yearMatches = sessionDate.year == state.firstVisibleMonth.yearMonth.year
            val monthMatches =
                sessionDate.month.toKotlinMonth() == state.firstVisibleMonth.yearMonth.month
            yearMatches && monthMatches
        }
    }

    var monthPickerOpen by remember { mutableStateOf(false) }

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

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { monthPickerOpen = true }
                    )
                    .padding(
                        vertical = dimensionResource(id = R.dimen.padding_small),
                        horizontal = dimensionResource(id = R.dimen.padding_medium)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$monthString $yearString"
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
                if (monthPickerOpen) {
                    MonthPicker(
                        onDismissRequest = { monthPickerOpen = false },
                        year = currentMonth.year,
                        onMonthClick = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(it)
                            }
                        },
                        startMonth = startMonth,
                        endMonth = endMonth
                    )
                }
            }
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(
                            currentMonth
                        )
                    }
                },
                enabled = todayButtonVisible,
                modifier = Modifier.align(Alignment.CenterEnd)
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
                    Box(
                        modifier = Modifier.matchParentSize()
                    ) {
                        if (addingSessions && day.position == DayPosition.MonthDate && day.date <= today) {
                            Box(
                                modifier = Modifier
                                    .clickable(
                                        indication = ripple(color = MaterialTheme.colorScheme.primary),
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        timestamp = day.date
                                            .atStartOfDayIn(zoneId.toKotlinTimeZone())
                                            .toJavaInstant()
                                        addSessionDialogOpen = true
                                    }
                                    .matchParentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.add),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        if (hasSessions) {
                            Box(
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                sessionsOnDay.take(3).forEachIndexed { index, session ->
                                    val workout = allWorkouts.find { it.id == session.workoutId }
                                    var visible by remember { mutableStateOf(false) }
                                    val workoutIndex = if (workout?.type == WorkoutType.GYM) {

                                        gymWorkouts.indexOf(workout)
                                    } else
                                        cardioWorkouts.indexOf(workout)

                                    LaunchedEffect(Unit) {
                                        delay(200 * index.toLong())
                                        visible = true
                                    }

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = visible,
                                        enter = slideInHorizontally(
                                            initialOffsetX = { -it / 2 },
                                        ) + fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Icon(
                                            painter =
                                                if (workout?.type == WorkoutType.GYM)
                                                    painterResource(id = R.drawable.weight)
                                                else
                                                    painterResource(id = R.drawable.run),
                                            tint = highlightColors[workoutIndex % highlightColors.size],
                                            contentDescription = stringResource(id = R.string.icon),
                                            modifier = Modifier
                                                .padding(start = dimensionResource(id = R.dimen.padding_medium) * index)
                                                .alpha(1f - index.toFloat() / 3)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        CalendarFooter(
            gymWorkouts = gymWorkouts,
            cardioWorkouts = cardioWorkouts,
            workoutSessions = sessionsForMonth,
            addingSessions = addingSessions,
            onAddSessionsClick = { addingSessions = !addingSessions },
            addingSessionsEnabled = allWorkouts.isNotEmpty()
        )
    }

    if (sessionDialogOpen) {
        Dialog(
            onDismissRequest = { sessionDialogOpen = false }
        ) {
            ElevatedCard {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(.5f)
                ) {
                    itemsIndexed(sessionsForDialog) { index, session ->
                        val workout = allWorkouts.find { it.id == session.workoutId }

                        WorkoutListItem(
                            workout = WorkoutWithLatestTimestamp(
                                id = workout!!.id,
                                name = workout.name,
                                latestTimestamp = session.timestamp
                            ),
                            onClick = { onSessionClick(session.id, workout.type) }
                        )

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

    if (addSessionDialogOpen) {
        Dialog(
            onDismissRequest = { addSessionDialogOpen = false }
        ) {
            ElevatedCard {
                LazyColumn(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_large))
                        .fillMaxHeight(.5f)
                ) {
                    itemsIndexed(allWorkouts) { index, workout ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    timestamp?.let { onAddSessionClick(workout, it) }
                                }
                        ) {
                            Icon(
                                painter =
                                    if (workout.type == WorkoutType.GYM)
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
fun MonthPicker(
    onDismissRequest: () -> Unit,
    year: Int,
    startMonth: kotlinx.datetime.YearMonth,
    endMonth: kotlinx.datetime.YearMonth,
    onMonthClick: (month: kotlinx.datetime.YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthPickerYear by remember(year) { mutableIntStateOf(year) }
    val calendarStartYear = startMonth.year
    val calendarEndYear = endMonth.year

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 80),
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 400.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimensionResource(id = R.dimen.padding_medium)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (monthPickerYear > calendarStartYear) monthPickerYear-- },
                        enabled = monthPickerYear > calendarStartYear
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(id = R.string.previous)
                        )
                    }
                    Text(
                        text = monthPickerYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (monthPickerYear < calendarEndYear) monthPickerYear++ },
                        enabled = monthPickerYear < calendarEndYear
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(id = R.string.next)
                        )
                    }
                }
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val monthsInPickerYear = Month.entries
                    items(monthsInPickerYear) { month ->
                        val targetYearMonth = YearMonth
                            .of(monthPickerYear, month)
                            .toKotlinYearMonth()
                        val isEnabled = targetYearMonth >= startMonth && targetYearMonth <= endMonth

                        TextButton(
                            onClick = {
                                onMonthClick(targetYearMonth)
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isEnabled,
                        ) {
                            Text(
                                text = month.getDisplayName(
                                    TextStyle.FULL_STANDALONE,
                                    Locale.getDefault()
                                ),
                                color =
                                    if (isEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
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
    addingSessions: Boolean,
    addingSessionsEnabled: Boolean,
    onAddSessionsClick: () -> Unit,
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
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider()
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
        }
        WorkoutLegendsRow(
            workouts = cardioWorkouts,
            sessionsByWorkoutId = sessionsByWorkoutId
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
            )
            val addSessionIconRotation by animateFloatAsState(
                targetValue = if (addingSessions) 45f else 0f,
                label = "Icon Rotation"
            )

            TextButton(
                onClick = onAddSessionsClick,
                enabled = addingSessionsEnabled,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription =
                        if (addingSessions) stringResource(id = R.string.close)
                        else stringResource(id = R.string.add),
                    modifier = Modifier.rotate(addSessionIconRotation)
                )
                Text(
                    text =
                        if (addingSessions) stringResource(id = R.string.close)
                        else stringResource(id = R.string.add)
                )
            }
        }
    }
}

@Composable
fun WorkoutLegendsRow(
    workouts: List<Workout>,
    sessionsByWorkoutId: Map<Int, List<WorkoutSession>>,
    modifier: Modifier = Modifier
) {
    val maxItemsInRow = 3
    FlowRow(
        maxItemsInEachRow = maxItemsInRow,
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
        if (workouts.size % maxItemsInRow != 0) {
            Spacer(modifier = Modifier.weight(1f))
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
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
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
                    Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium))
                        )
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
        Text(
            text = day.date.day.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = dimensionResource(id = R.dimen.padding_small))
        )
    }
}

@Composable
private fun PieChartCard(
    workouts: List<Workout>,
    workoutSessions: List<WorkoutSession>,
    type: WorkoutType,
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
            }.ifEmpty {
                listOf(
                    Pie(
                        label = "",
                        data = 1.0,
                        color = Color.Gray
                    )
                )
            }
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.padding_medium)
        ),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter =
                        if (type == WorkoutType.GYM)
                            painterResource(id = R.drawable.weight)
                        else
                            painterResource(id = R.drawable.run),
                    contentDescription = stringResource(id = R.string.icon),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
                PieChart(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(200.dp),
                    data = data,
                    spaceDegree = 2f,
                    style = Pie.Style.Stroke(width = 20.dp)
                )
            }
            WorkoutLegendsRow(
                workouts = workouts,
                sessionsByWorkoutId = sessionsByWorkoutId,
                modifier = Modifier
            )
            Text(
                text = "${stringResource(id = R.string.total)}: ${workoutSessions.size}"
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
    val cardioWorkouts = List(5) {
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

    val gymSessions = List(200) {
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
        onSessionNavigate = { _, _ -> },
        onWorkoutStatsNavigate = {},
        onAddSessionNavigate = { _, _ -> }
    )
}

@Preview(showBackground = true, device = "spec:width=1080px,height=4000px,dpi=440")
@Composable
private fun StatsOverviewPreview() {
    GymTrackerTheme {
        Surface {
            StatsScreenForPreview()
        }
    }
}

@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "fi",
    device = "spec:width=1080px,height=4000px,dpi=440"
)
@Composable
private fun StatsOverviewPreviewDark() {
    GymTrackerTheme {
        Surface {
            StatsScreenForPreview()
        }
    }
}
