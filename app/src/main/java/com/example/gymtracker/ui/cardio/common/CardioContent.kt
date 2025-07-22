package com.example.gymtracker.ui.cardio.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.NumericTextField
import com.example.gymtracker.utility.toDateAndTimeString
import com.example.gymtracker.utility.toReadableString
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant


@Composable
fun CardioContent(
    previousSteps: Int? = null,
    previousStepsTimestamp: Instant? = null,
    steps: Int? = 0,
    onStepsChange: (steps: Int) -> Unit = {},
    previousDistance: Double? = null,
    previousDistanceTimestamp: Instant? = null,
    distance: Double? = 0.0,
    onDistanceChange: (distance: Double) -> Unit = {},
    previousDuration: Duration? = null,
    previousDurationTimestamp: Instant? = null,
    onDurationChange: (duration: Duration) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.steps)
                )
            },
            lastTimeInfo = {
                if (previousSteps != null && previousStepsTimestamp != null) {
                    LastTimeRow(
                        valueText = {
                            Text(
                                text = previousSteps.toString()
                            )
                        },
                        dateText = {
                            Text(
                                text = previousStepsTimestamp.toDateAndTimeString()
                            )
                        }
                    )
                }
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NumericTextField(
                    value = steps,
                    onValueChange = onStepsChange
                )
                Text(
                    text = stringResource(id = R.string.steps)
                )
            }
        }
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.distance)
                )
            },
            lastTimeInfo = {
                if (previousDistance != null && previousDistanceTimestamp != null) {
                    LastTimeRow(
                        valueText = {
                            Text(
                                text = "${previousDistance}km"
                            )
                        },
                        dateText = {
                            Text(
                                text = previousDistanceTimestamp.toDateAndTimeString()
                            )
                        }
                    )
                }
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NumericTextField(
                    value = distance,
                    onValueChange = onDistanceChange
                )
                Text(
                    text = "km"
                )
            }
        }
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.time)
                )
            },
            lastTimeInfo = {
                if (previousDuration != null && previousDurationTimestamp != null) {
                    LastTimeRow(
                        valueText = {
                            Text(
                                text = previousDuration.toReadableString()
                            )
                        },
                        dateText = {
                            Text(
                                text = previousDurationTimestamp.toDateAndTimeString()
                            )
                        }
                    )
                }
            }
        ) {
            StopWatch(
                onPause = { onDurationChange(Duration.ofMillis(it)) },
                onStop = { onDurationChange(Duration.ofMillis(it)) }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val milliseconds = ms % 1000
    return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds)
}

@Composable
private fun StopWatch(
    onPause: (millis: Long) -> Unit,
    onStop: (millis: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val amount = 16L

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(amount)
            timeMillis += amount
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = formatTime(timeMillis),
            style = MaterialTheme.typography.displayMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (isRunning) {
                IconButton(
                    onClick = {
                        isRunning = false
                        onPause(timeMillis)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timer_pause),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { isRunning = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timer_play),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            IconButton(
                onClick = {
                    isRunning = false
                    timeMillis = 0L
                    onStop(timeMillis)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stop_circle),
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun CardioCard(
    title: @Composable () -> Unit,
    lastTimeInfo: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                title()
                lastTimeInfo()
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            content()
        }
    }
}

@Composable
private fun LastTimeRow(
    valueText: @Composable () -> Unit,
    dateText: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)
        ) {
            valueText()
            Icon(
                painter = painterResource(id = R.drawable.history),
                contentDescription = null
            )
            dateText()
        }
    }
}
