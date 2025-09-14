package com.tonicantarella.gymtracker.ui.cardio.cardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.cardio.common.CardioContent
import com.tonicantarella.gymtracker.ui.common.FinishWorkoutDialog
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.navigation.NavigationGuardController
import com.tonicantarella.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioWorkoutScreen(
    onNavigateBack: () -> Unit,
    navigationGuard: NavigationGuardController,
    viewModel: CardioWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges = uiState.hasUnsavedChanges
    val hasMarkedMetrics = uiState.hasMarkedMetrics
    val hasChanges = hasUnsavedChanges || hasMarkedMetrics

    var finishWorkoutDialogOpen by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(hasChanges) {
        navigationGuard.guard(hasChanges)
    }

    fun onFinishWorkout() {
        navigationGuard.release()
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
        navigationGuard.release()
        viewModel.onSaveChanges()
        onNavigateBack()
    }

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopBarTextField(
                        value = uiState.workout.name,
                        onValueChange = viewModel::onNameChange,
                        maxSize = CARDIO_NAME_MAX_SIZE
                    )
                },
                navigationIcon = {
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
        },
        floatingActionButton = {
            val enabled = hasChanges
            GymFloatingActionButton(
                enabled = enabled,
                onClick = {
                    if (hasMarkedMetrics)
                        finishWorkoutCheck()
                    else
                        saveChanges()
                }
            ) {
                if (hasMarkedMetrics) {
                    Icon(
                        painter = painterResource(id = R.drawable.goal),
                        contentDescription = stringResource(id = R.string.done)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.save),
                        contentDescription = stringResource(id = R.string.save)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
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
        }
    }

    if (finishWorkoutDialogOpen) {
        FinishWorkoutDialog(
            onCancel = { finishWorkoutDialogOpen = false },
            onFinishWorkout = { doNotAskAgain ->
                finishWorkoutDialogOpen = false
                if (doNotAskAgain) {
                    viewModel.stopAskingFinishConfirm()
                }
                onFinishWorkout()
            }
        )
    }
}