package com.example.gymtracker.ui.stats.overview

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.entity.statsoverview.CalendarLegend
import com.example.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.example.gymtracker.ui.entity.statsoverview.WorkoutType
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
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
import kotlinx.datetime.toKotlinTimeZone
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
import kotlin.time.toJavaInstant

@Composable
fun StatsOverviewScreen(
    onNavigateBack: () -> Unit,
    onSessionNavigate: (id: Int) -> Unit,
    onAddSessionNavigate: (workout: WorkoutWithTimestamp, timestamp: Instant) -> Unit,
    onWorkoutStatsNavigate: (workout: WorkoutWithTimestamp) -> Unit,
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
        calendarSessions = uiState.calendarSessions,
        calendarLegends = uiState.calendarLegends,
        getMonthData = viewModel::getMonthData,
        onSessionNavigate = onSessionNavigate,
        onAddSessionNavigate = onAddSessionNavigate,
        onWorkoutStatsNavigate = onWorkoutStatsNavigate,
        colorIndexMap = uiState.colorIndexMap
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
    gymSessions: List<WorkoutSession>,
    cardioSessions: List<WorkoutSession>,
    calendarSessions: Map<LocalDate, List<WorkoutSession>>,
    calendarLegends: List<CalendarLegend>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onSessionNavigate: (id: Int) -> Unit,
    onAddSessionNavigate: (workout: WorkoutWithTimestamp, timestamp: Instant) -> Unit,
    onWorkoutStatsNavigate: (workout: WorkoutWithTimestamp) -> Unit,
    colorIndexMap: Map<Int, Int>
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
                    sessionsForMonth = calendarSessions,
                    legends = calendarLegends,
                    getMonthData = getMonthData,
                    onSessionClick = onSessionNavigate,
                    onAddSessionClick = onAddSessionNavigate,
                    colorIndexMap = colorIndexMap,
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
                            modifier = Modifier
                                .width(itemWidth)
                                .height(itemHeight)
                        )
                    }
                    item {
                        PieChartCard(
                            workouts = cardioWorkouts.ifEmpty { emptyList() },
                            workoutSessions = cardioSessions.ifEmpty { emptyList() },
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
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
    onWorkoutNavigate: (workout: WorkoutWithTimestamp) -> Unit,
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
    workout: WorkoutWithTimestamp,
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
                    painter = painterResource(id = R.drawable.weight),
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

/* TODO
    Split into smaller composables, unreadable now
    when you do, order everything below and above in some sane order
* */
@OptIn(ExperimentalTime::class)
@Composable
private fun StatCalendar(
    sessionsForMonth: Map<LocalDate, List<WorkoutSession>>,
    getMonthData: (startDate: Instant, endDate: Instant) -> Unit,
    onSessionClick: (id: Int) -> Unit,
    onAddSessionClick: (workout: WorkoutWithTimestamp, timestamp: Instant) -> Unit,
    legends: List<CalendarLegend>,
    colorIndexMap: Map<Int, Int>,
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
    val yearString = state.firstVisibleMonth.yearMonth.year.toString()

    var sessionDialogOpen by remember { mutableStateOf(false) }
    val todayButtonVisible = currentMonth != state.firstVisibleMonth.yearMonth

    // TODO needed?
    /*val allWorkouts = gymWorkouts + cardioWorkouts
    var sessionsForDialog: List<WorkoutSession> by remember { mutableStateOf(emptyList()) }
    val sessionsByDate: Map<LocalDate, List<WorkoutSession>> = remember(sessionsForMonth) {
        sessionsForMonth.groupBy {
            it.timestamp.atZone(zoneId).toLocalDate().toKotlinLocalDate()
        }
    }*/



    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth }
            .distinctUntilChanged()
            .collect { visibleMonth ->
                // TODO ew
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
        CalendarHeader(
            monthString = monthString,
            yearString = yearString,
            yearInt = currentMonth.year,
            todayButtonVisible = todayButtonVisible,
            startMonth = startMonth,
            endMonth = endMonth,
            onTodayClick = {
                coroutineScope.launch {
                    state.animateScrollToMonth(
                        currentMonth
                    )
                }
            },
            onMonthClick = {
                coroutineScope.launch {
                    state.animateScrollToMonth(it)
                }
            }
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
                val sessionsOnDay = sessionsForMonth[day.date]
                val hasSessions = sessionsOnDay?.isNotEmpty() == true

                fun onClick() {
                    if (sessionsOnDay?.size!! == 1) {
                        val session = sessionsOnDay.first()
                        onSessionClick(session.sessionId)
                    } else {
                        sessionDialogOpen = true
                        //sessionsForDialog = sessionsOnDay
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
                                        // TODO ew
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
                                sessionsOnDay.distinctBy { it.workoutId }.take(3)
                                    .forEachIndexed { index, session ->
                                        var visible by remember { mutableStateOf(false) }
                                        val colorIndex = colorIndexMap[session.workoutId] ?: 0

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
                                                    if (session.type == WorkoutType.GYM)
                                                        painterResource(id = R.drawable.weight)
                                                    else
                                                        painterResource(id = R.drawable.run),
                                                tint = highlightColors[colorIndex],
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
        //TODO
        /*CalendarFooter(
            gymWorkouts = gymWorkouts,
            cardioWorkouts = cardioWorkouts,
            workoutSessions = sessionsForMonth,
            addingSessions = addingSessions,
            onAddSessionsClick = { addingSessions = !addingSessions },
            addingSessionsEnabled = allWorkouts.isNotEmpty()
        )*/
    }

    if (sessionDialogOpen) {
        //TODO
        /*Dialog(
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
                            workout = WorkoutWithTimestamp(
                                id = workout!!.id,
                                name = workout.name,
                                timestamp = session.timestamp
                            ),
                            onClick = { onSessionClick(session.id) }
                        )

                        if (sessionsForDialog.size > 1 && index != sessionsForDialog.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }*/
    }

    if (addSessionDialogOpen) {
        /* TODO
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
                                    // TODO
                                        painterResource(id = R.drawable.weight),
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
        }*/
    }
}

@Composable
fun CalendarHeader(
    monthString: String,
    yearString: String,
    todayButtonVisible: Boolean,
    startMonth: kotlinx.datetime.YearMonth,
    endMonth: kotlinx.datetime.YearMonth,
    yearInt: Int,
    onTodayClick: () -> Unit,
    onMonthClick: (kotlinx.datetime.YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthPickerOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            val monthPickerButtonWidth = 160.dp
            TextButton(
                onClick = { monthPickerOpen = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.width(monthPickerButtonWidth)
            ) {
                Text(
                    text = "$monthString $yearString"
                )

                val arrowRotation by animateFloatAsState(
                    targetValue = if (monthPickerOpen) 180f else 0f,
                    label = "Icon Rotation"
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }
            MonthPicker(
                expanded = monthPickerOpen,
                onDismissRequest = { monthPickerOpen = false },
                anchorButtonWidth = monthPickerButtonWidth,
                year = yearInt,
                onMonthClick = onMonthClick,
                startMonth = startMonth,
                endMonth = endMonth
            )
        }
        TextButton(
            onClick = onTodayClick,
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
}

@Composable
fun MonthPicker(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchorButtonWidth: Dp,
    year: Int,
    startMonth: kotlinx.datetime.YearMonth,
    endMonth: kotlinx.datetime.YearMonth,
    onMonthClick: (month: kotlinx.datetime.YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthPickerYear by remember(year, expanded) { mutableIntStateOf(year) }
    val calendarStartYear = startMonth.year
    val calendarEndYear = endMonth.year

    val menuWidth = 300.dp
    val horizontalOffSetDp = -(anchorButtonWidth / 2 - menuWidth / 2)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(horizontalOffSetDp, 0.dp),
        modifier = modifier
            .width(menuWidth)
            .heightIn(max = 400.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_medium))
                    .fillMaxWidth()
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
                    text = "$monthPickerYear",
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
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val monthsInPickerYear = java.time.Month.entries
                monthsInPickerYear.forEach { month ->
                    val targetYearMonth = YearMonth
                        .of(monthPickerYear, month)
                        .toKotlinYearMonth()

                    val isEnabled = targetYearMonth in startMonth..endMonth

                    DropdownMenuItem(
                        text = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
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
                        },
                        onClick = {
                            onMonthClick(targetYearMonth)
                            onDismissRequest()
                        },
                        enabled = isEnabled
                    )
                }
            }
        }
    }
}


@Composable
fun CalendarFooter(
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
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
    workouts: List<WorkoutWithTimestamp>,
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
    workout: WorkoutWithTimestamp,
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
                // TODO
                painterResource(id = R.drawable.weight),
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
    workouts: List<WorkoutWithTimestamp>,
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val sessionsByWorkoutId = remember(workoutSessions) {
        workoutSessions.groupBy { it.workoutId }
    }

    var data by remember {
        mutableStateOf(
            if (workouts.isNotEmpty() && workoutSessions.isNotEmpty()) {
                workouts.map { workout ->
                    val workoutIndex = workouts.indexOf(workout)
                    val amountOfSessions = sessionsByWorkoutId[workout.id]?.size?.toDouble() ?: 0.0
                    val color = highlightColors[workoutIndex % highlightColors.size]
                    Pie(
                        label = workout.name,
                        data = amountOfSessions,
                        color = color
                    )
                }
            } else {
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
                    // TODO
                    painter = painterResource(id = R.drawable.weight),
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

/* TODO
@Composable
private fun StatsScreenForPreview() {
    val gymWorkouts = List(MAX_GYM_WORKOUTS) {
        val name =
            if (it == 0)
                "This is a very long name that can overflow"
            else
                "Gym ${it + 1}"
        WorkoutWithTimestamp(
            id = it,
            name = name,
            timestamp = Instant.now()
        )
    }
    val cardioWorkouts = List(5) {
        val name =
            if (it == 0)
                "This is a very long name that can overflow"
            else
                "Cardio ${it + 1}"
        WorkoutWithTimestamp(
            id = it + MAX_GYM_WORKOUTS,
            name = name,
            timestamp = Instant.now()
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
        onSessionNavigate = { _ -> },
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
}*/
