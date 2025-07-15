package com.example.gymtracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
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
        workoutDays = uiState.workoutDays
    )
}

@Composable
private fun StatsOverviewScreen(
    loading: Boolean,
    workoutDays: List<Instant>
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
                    days = workoutDays
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
    days: List<Instant>,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.systemDefault()
    val highlightedDates: List<LocalDate> = remember(days) {
        days.map { it.atZone(zoneId).toLocalDate().toKotlinLocalDate() }
    }

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

    StatsCard(modifier = modifier) {
        Text(
            text = currentMonth.month.getDisplayName(
                java.time.format.TextStyle.FULL,
                Locale.getDefault()
            ),
            modifier = Modifier.padding(
                bottom = dimensionResource(id = R.dimen.padding_medium),
                start = dimensionResource(id = R.dimen.padding_medium)
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
            dayContent = { day -> Day(day, selected = highlightedDates.contains(day.date)) }
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small))
                .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Green)
                        .size(10.dp)
                )
                Text(
                    text = stringResource(id = R.string.days_worked_out)
                )
            }
            Text(
                text = "${stringResource(id = R.string.total)}: ${days.size}"
            )
        }
    }
}

val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

@Composable
private fun Day(day: CalendarDay, selected: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = if (selected) Color.Green.copy(alpha = .3f) else Color.Transparent)
            .then(
                if (day.date == today) Modifier.border(2.dp, Color.Red, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate && day.date <= today) Color.White else Color.Gray
        )
    }
}

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