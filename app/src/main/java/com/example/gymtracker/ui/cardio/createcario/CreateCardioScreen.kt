package com.example.gymtracker.ui.cardio.createcario

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.ui.workouts.CARDIO_NAME_MAX_SIZE
import com.example.gymtracker.utility.toDateAndTimeString
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant

@Composable
fun CreateCardioScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateCardioViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            TopBarTextField(
                value = uiState.name,
                onValueChange = viewModel::onChangeName,
                maxSize = CARDIO_NAME_MAX_SIZE
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = {
            viewModel.onSavePressed { onNavigateBack() }
        },
        visible = true
    ) {
        Icon(
            painter = painterResource(id = R.drawable.save),
            contentDescription = null
        )
    }
    Box{
        CardioContent()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ){}
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = .5f)
                )
        )
    }
}

@Composable
private fun CardioContent(
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
            subtitle = {
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
                OutlinedTextField(
                    value = steps.toString(),
                    onValueChange = {
                        onStepsChange(it.toIntOrNull() ?: 0)
                    },
                    modifier = Modifier.width(60.dp)
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
            subtitle = {
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
                OutlinedTextField(
                    value = distance.toString(),
                    onValueChange = {
                        onDistanceChange(it.toDoubleOrNull() ?: 0.0)
                    },
                    modifier = Modifier.width(60.dp)
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
            subtitle = {
                if (previousDuration != null && previousDurationTimestamp != null) {
                    LastTimeRow(
                        valueText = {
                            Text(
                                text = "${previousDuration}min"
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
                onStop = { onDurationChange(Duration.ofMillis(it))  }
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
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            title()
            subtitle()
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
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = Modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(
                alpha = .6f
            )
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

@Preview(showBackground = true)
@Composable
private fun CreateCardioPreview() {
    GymTrackerTheme {
        CardioContent(
            steps = 0,
            onStepsChange = {},
            distance = 0.0,
            onDistanceChange = {},
            onDurationChange = {}
        )
    }
}