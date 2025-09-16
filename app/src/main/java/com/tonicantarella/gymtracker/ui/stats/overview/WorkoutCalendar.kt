package com.tonicantarella.gymtracker.ui.stats.overview

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutLegend
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutSession
import com.tonicantarella.gymtracker.ui.entity.statsoverview.WorkoutType
import com.tonicantarella.gymtracker.utility.toDateAndTimeString
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
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

@OptIn(ExperimentalTime::class)
@Composable
fun WorkoutCalendar(
    sessionsForMonth: Map<LocalDate, List<WorkoutSession>>,
    getMonthSessions: (startDate: Instant, endDate: Instant) -> Unit,
    onGymSessionClick: (id: Int) -> Unit,
    onCardioSessionClick: (id: Int) -> Unit,
    onAddGymSessionClick: (workoutId: Int, timestamp: Instant) -> Unit,
    onAddCardioSessionClick: (workoutId: Int, timestamp: Instant) -> Unit,
    gymLegends: List<WorkoutLegend>,
    cardioLegends: List<WorkoutLegend>,
    gymColorIndexMap: Map<Int, Int>,
    cardioColorIndexMap: Map<Int, Int>,
    gymWorkouts: List<WorkoutWithTimestamp>,
    cardioWorkouts: List<WorkoutWithTimestamp>,
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

    var daySessionDialogOpen by remember { mutableStateOf(false) }
    val todayButtonVisible = currentMonth != state.firstVisibleMonth.yearMonth

    var daySessionsForDialog: List<WorkoutSession> by remember { mutableStateOf(emptyList()) }

    fun onSessionClick(session: WorkoutSession) {
        if (session.type == WorkoutType.GYM) {
            onGymSessionClick(session.sessionId)
        } else {
            onCardioSessionClick(session.sessionId)
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

                getMonthSessions(startDate, endDate)
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
                WeekdayRow(
                    daysOfWeek = daysOfWeek,
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )
            },
            dayContent = { day ->
                val sessionsOnDay = sessionsForMonth[day.date]
                val hasSessions = sessionsOnDay?.isNotEmpty() == true

                fun onClick() {
                    if (sessionsOnDay?.size!! == 1) {
                        val session = sessionsOnDay.first()

                        onSessionClick(session)
                    } else {
                        daySessionDialogOpen = true
                        daySessionsForDialog = sessionsOnDay
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
                            DayAddButton(
                                onClick = {
                                    timestamp = day.date
                                        .atStartOfDayIn(zoneId.toKotlinTimeZone())
                                        .toJavaInstant()
                                    addSessionDialogOpen = true
                                },
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        if (sessionsOnDay?.isNotEmpty() == true) {
                            val gymSessions = sessionsOnDay
                                .filter { it.type == WorkoutType.GYM }
                                .distinctBy { it.workoutId }
                                .take(3)
                            val cardioSessions = sessionsOnDay
                                .filter { it.type == WorkoutType.CARDIO }
                                .distinctBy { it.workoutId }
                                .take(3)

                            DayWorkoutIcons(
                                gymSessions = gymSessions,
                                cardioSessions = cardioSessions,
                                gymColorIndexMap = gymColorIndexMap,
                                cardioColorIndexMap = cardioColorIndexMap,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                            )
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        CalendarFooter(
            gymLegends = gymLegends,
            cardioLegends = cardioLegends,
            gymColorIndexMap = gymColorIndexMap,
            cardioColorIndexMap = cardioColorIndexMap,
            addingSessions = addingSessions,
            onAddSessionsClick = { addingSessions = !addingSessions },
            addingSessionsEnabled = (gymLegends + cardioLegends).isNotEmpty()
        )
    }

    if (daySessionDialogOpen) {
        Dialog(
            onDismissRequest = { daySessionDialogOpen = false }
        ) {
            ElevatedCard {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(.5f)
                ) {
                    itemsIndexed(daySessionsForDialog) { index, session ->
                        if (session.type == WorkoutType.GYM) {
                            WorkoutDialogItem(
                                onClick = { onSessionClick(session) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.weight),
                                        contentDescription = null,
                                        tint = highlightColors[gymColorIndexMap[session.workoutId]
                                            ?: 0]
                                    )
                                },
                                workoutName = session.workoutName,
                                timestamp = session.timestamp.toDateAndTimeString()
                            )
                        } else {
                            WorkoutDialogItem(
                                onClick = { onSessionClick(session) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.run),
                                        contentDescription = null,
                                        tint = highlightColors[cardioColorIndexMap[session.workoutId]
                                            ?: 0]
                                    )
                                },
                                workoutName = session.workoutName,
                                timestamp = session.timestamp.toDateAndTimeString()
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
                        .fillMaxHeight(.5f)
                ) {
                    itemsIndexed(gymWorkouts) { index, workout ->
                        WorkoutDialogItem(
                            onClick = { timestamp?.let { onAddGymSessionClick(workout.id, it) } },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.weight),
                                    contentDescription = null,
                                    tint = highlightColors[gymColorIndexMap[workout.id] ?: 0]
                                )
                            },
                            workoutName = workout.name
                        )
                    }
                    itemsIndexed(cardioWorkouts) { index, workout ->
                        WorkoutDialogItem(
                            onClick = {
                                timestamp?.let {
                                    onAddCardioSessionClick(
                                        workout.id,
                                        it
                                    )
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.run),
                                    contentDescription = null,
                                    tint = highlightColors[cardioColorIndexMap[workout.id] ?: 0]
                                )
                            },
                            workoutName = workout.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDialogItem(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    workoutName: String,
    modifier: Modifier = Modifier,
    timestamp: String? = null
) {
    Surface(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Column {
                    Text(
                        text = workoutName
                    )
                    if (timestamp != null) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.history),
                                contentDescription = null
                            )
                            Text(
                                text = timestamp
                            )
                        }
                    }
                }
            }
        }
    }
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
            .padding(1.dp)
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
            .aspectRatio(1f)

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
                            width = 2.dp,
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
private fun DayAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.add),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun DayWorkoutIcons(
    gymSessions: List<WorkoutSession>,
    cardioSessions: List<WorkoutSession>,
    gymColorIndexMap: Map<Int, Int>,
    cardioColorIndexMap: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val delay = 200L

    Box(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                gymSessions.forEachIndexed { index, session ->
                    var visible by remember { mutableStateOf(false) }
                    val colorIndex = gymColorIndexMap[session.workoutId] ?: 0

                    LaunchedEffect(Unit) {
                        delay(delay * index.toLong())
                        visible = true
                    }

                    AnimatedWorkoutIcon(
                        visible = visible,
                        painter = painterResource(id = R.drawable.weight),
                        tint = highlightColors[colorIndex],
                        modifier = Modifier
                            .padding(start = dimensionResource(id = R.dimen.padding_medium) * index)
                            .alpha(1f - index.toFloat() / 3)
                    )
                }
            }
            Box {
                cardioSessions.forEachIndexed { index, session ->
                    var visible by remember { mutableStateOf(false) }
                    val colorIndex = cardioColorIndexMap[session.workoutId] ?: 0

                    LaunchedEffect(Unit) {
                        delay((gymSessions.size * delay) + (delay * index.toLong()))
                        visible = true
                    }

                    AnimatedWorkoutIcon(
                        visible = visible,
                        painter = painterResource(id = R.drawable.run),
                        tint = highlightColors[colorIndex],
                        modifier = Modifier
                            .padding(start = dimensionResource(id = R.dimen.padding_medium) * index)
                            .alpha(1f - index.toFloat() / 3)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayRow(
    daysOfWeek: List<DayOfWeek>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
}

@Composable
private fun AnimatedWorkoutIcon(
    visible: Boolean,
    painter: Painter,
    tint: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it / 2 },
        ) + fadeIn(),
        exit = fadeOut()
    ) {
        Icon(
            painter = painter,
            tint = tint,
            contentDescription = stringResource(id = R.string.icon),
            modifier = modifier
        )
    }
}

@Composable
private fun CalendarHeader(
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
private fun MonthPicker(
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

    val menuWidth = 350.dp
    val horizontalOffSetDp = -(menuWidth / 2 - anchorButtonWidth / 2)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(horizontalOffSetDp, 0.dp),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .width(menuWidth)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
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
private fun CalendarFooter(
    gymLegends: List<WorkoutLegend>,
    cardioLegends: List<WorkoutLegend>,
    gymColorIndexMap: Map<Int, Int>,
    cardioColorIndexMap: Map<Int, Int>,
    addingSessions: Boolean,
    addingSessionsEnabled: Boolean,
    onAddSessionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = remember {
        gymLegends.sumOf { it.sessionCount } + cardioLegends.sumOf { it.sessionCount }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        WorkoutLegendsRow(
            legends = gymLegends,
            colorIndexMap = gymColorIndexMap
        )
        if (gymLegends.isNotEmpty() && cardioLegends.isNotEmpty()) {
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider()
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
        }
        WorkoutLegendsRow(
            legends = cardioLegends,
            colorIndexMap = cardioColorIndexMap
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${stringResource(id = R.string.total)}: $total"
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