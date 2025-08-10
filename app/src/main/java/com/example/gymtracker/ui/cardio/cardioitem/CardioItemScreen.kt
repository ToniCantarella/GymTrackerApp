package com.example.gymtracker.ui.cardio.cardioitem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideNavigationBarGuard
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.navigation.rememberProceedOnGuardCleared
import com.example.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioItemScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStats: (id: Int) -> Unit,
    viewModel: CardioItemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var backNavigationDialog by remember { mutableStateOf(false) }
    var dialogNavigationAction: () -> Unit by remember { mutableStateOf({}) }

    val hasUnsavedChanges =
        uiState.cardio != uiState.initialCardio

    fun navigationCheck(onNavigate: () -> Unit) {
        if (hasUnsavedChanges) {
            backNavigationDialog = true
            dialogNavigationAction = onNavigate
        } else {
            onNavigate()
        }
    }

    BackHandler {
        navigationCheck { onNavigateBack() }
    }

    ProvideNavigationBarGuard(
        isGuarded = hasUnsavedChanges,
        onGuard = { backNavigationDialog = true }
    )
    val proceedOnGuardCleared = rememberProceedOnGuardCleared()

    ProvideTopAppBar(
        title = {
            TopBarTextField(
                value = uiState.cardio.name,
                onValueChange = viewModel::onChangeName,
                maxSize = CARDIO_NAME_MAX_SIZE
            )
        },
        navigationItem = {
            IconButton(
                onClick = { navigationCheck { onNavigateBack() } }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = { navigationCheck { onNavigateToStats(uiState.cardioId) } }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.timeline),
                    contentDescription = stringResource(R.string.stats)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = { viewModel.onFinishPressed { onNavigateBack() } }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.goal),
            contentDescription = stringResource(id = R.string.done)
        )
    }

    if (uiState.loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else {
        CardioContent(
            steps = uiState.cardio.steps,
            onStepsChange = viewModel::onStepsChange,
            previousSteps = uiState.previousCardio?.steps,
            previousStepsTimestamp = uiState.previousCardio?.stepsTimestamp,
            distance = uiState.cardio.distance,
            previousDistance = uiState.previousCardio?.distance,
            onDistanceChange = viewModel::onDistanceChange,
            previousDistanceTimestamp = uiState.previousCardio?.distanceTimestamp,
            previousDuration = uiState.previousCardio?.duration,
            previousDurationTimestamp = uiState.previousCardio?.durationTimestamp,
            onDurationChange = viewModel::onDurationChange
        )
    }

    if (backNavigationDialog) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(id = R.string.unsaved_changes),
                    textAlign = TextAlign.Center
                )
            },
            cancelButton = {
                OutlinedButton(
                    onClick = { backNavigationDialog = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backNavigationDialog = false
                        dialogNavigationAction.invoke()
                        proceedOnGuardCleared()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            onDismissRequest = { backNavigationDialog = false }
        )
    }
}