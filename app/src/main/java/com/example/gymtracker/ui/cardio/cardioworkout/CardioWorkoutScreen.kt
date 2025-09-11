package com.example.gymtracker.ui.cardio.cardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.common.ConfirmDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    releaseNavigationGuard: () -> Unit,
    viewModel: CardioWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges = uiState.hasUnsavedChanges
    val hasMarkedMetrics = uiState.hasMarkedMetrics

    var finishWorkoutDialogOpen by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(hasUnsavedChanges, hasMarkedMetrics) {
        onNavigationGuardChange(hasUnsavedChanges || hasMarkedMetrics)
    }

    fun onFinishWorkout() {
        releaseNavigationGuard()
        viewModel.onFinishPressed {
            onNavigateBack()
        }
    }

    fun finishWorkoutCheck() {
        if (hasMarkedMetrics && uiState.showFinishWorkoutDialog) {
            finishWorkoutDialogOpen = true
        } else {
            onFinishWorkout()
        }
    }

    fun saveChanges() {
        releaseNavigationGuard()
        viewModel.onSaveChanges()
        onNavigateBack()
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
            steps = uiState.workout.metrics.steps,
            onStepsChange = viewModel::onStepsChange,
            distance = uiState.workout.metrics.distance,
            onDistanceChange = viewModel::onDistanceChange,
            onDurationChange = viewModel::onDurationChange,
        )
    }

    if (finishWorkoutDialogOpen) {
        var doNotAskAgain by remember { mutableStateOf(false) }

        ConfirmDialog(
            subtitle = {
                Column {
                    Text(
                        text = stringResource(id = R.string.confirm_done_workout),
                        textAlign = TextAlign.Center
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            doNotAskAgain,
                            onCheckedChange = { doNotAskAgain = it }
                        )
                        Text(
                            text = stringResource(id = R.string.do_not_ask_again)
                        )
                    }
                }
            },
            cancelButton = {
                OutlinedButton(
                    onClick = { finishWorkoutDialogOpen = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        finishWorkoutDialogOpen = false
                        if (doNotAskAgain) {
                            viewModel.stopAskingFinishConfirm()
                        }
                        onFinishWorkout()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            onDismissRequest = { finishWorkoutDialogOpen = false }
        )
    }
}