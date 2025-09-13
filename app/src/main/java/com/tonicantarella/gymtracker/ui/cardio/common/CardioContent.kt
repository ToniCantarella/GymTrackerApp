package com.tonicantarella.gymtracker.ui.cardio.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.NumericTextField
import com.tonicantarella.gymtracker.utility.UnitUtil
import kotlinx.coroutines.delay
import java.time.Duration

@Composable
fun CardioContent(
    steps: Int? = 0,
    onStepsChange: (steps: Int) -> Unit = {},
    distance: Double? = 0.0,
    onDistanceChange: (distance: Double) -> Unit = {},
    displayDuration: Duration? = null,
    onDurationChange: (duration: Duration) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.steps)
                )
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.footprint),
                    contentDescription = stringResource(id = R.string.steps)
                )
                NumericTextField(
                    value = steps,
                    onValueChange = onStepsChange,
                    valueMaxLength = 6,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = stringResource(id = R.string.steps_count)
                )
            }
        }
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.distance)
                )
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.path),
                    contentDescription = stringResource(id = R.string.distance)
                )
                NumericTextField(
                    value = distance,
                    onValueChange = onDistanceChange,
                    valueMaxLength = 6,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = stringResource(UnitUtil.distanceUnitStringId)
                )
            }
        }
        CardioCard(
            title = {
                Text(
                    text = stringResource(id = R.string.time)
                )
            }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.timer),
                    contentDescription = stringResource(id = R.string.time),
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                StopWatch(
                    onPause = { onDurationChange(Duration.ofMillis(it)) },
                    onStop = { onDurationChange(Duration.ofMillis(it)) },
                    displayDuration = displayDuration
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun StopWatch(
    onPause: (millis: Long) -> Unit,
    onStop: (millis: Long) -> Unit,
    modifier: Modifier = Modifier,
    displayDuration: Duration? = null
) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var lastTickTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            val now = System.currentTimeMillis()
            elapsedMillis += (now - lastTickTime)
            lastTickTime = now
            delay(10L)
        }
    }

    val durationString = if (displayDuration != null) {
        val totalMillis = displayDuration.toMillis()
        val dispMinutes = (totalMillis / 1000) / 60
        val dispSeconds = (totalMillis / 1000) % 60
        val dispMillis = totalMillis % 1000
        String.format("%02d:%02d.%03d", dispMinutes, dispSeconds, dispMillis)
    } else {
        val hours = (elapsedMillis / 3600000)
        val minutes = (elapsedMillis / 60000) % 60
        val seconds = (elapsedMillis / 1000) % 60
        val millis = (elapsedMillis % 1000)
        String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = durationString,
            style = MaterialTheme.typography.displayMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (isRunning) {
                IconButton(
                    onClick = {
                        isRunning = false
                        onPause(elapsedMillis)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timer_pause),
                        contentDescription = stringResource(id = R.string.pause),
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        lastTickTime = System.currentTimeMillis()
                        isRunning = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timer_play),
                        contentDescription = stringResource(id = R.string.play),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            IconButton(
                onClick = {
                    isRunning = false
                    elapsedMillis = 0L
                    onStop(elapsedMillis)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stop_circle),
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = stringResource(id = R.string.stop),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun CardioCard(
    title: @Composable () -> Unit,
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
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            content()
        }
    }
}
