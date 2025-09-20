package com.tonicantarella.gymtracker.ui.cardio.cardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.cardio.common.CardioContent
import com.tonicantarella.gymtracker.ui.common.FinishWorkoutDialog
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.common.UnsavedChangesDialog
import com.tonicantarella.gymtracker.ui.stats.cardio.CardioWorkoutStatsList
import com.tonicantarella.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import com.tonicantarella.gymtracker.utility.toDateAndTimeString
import com.tonicantarella.gymtracker.utility.toDateString
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioWorkoutScreen(
    viewModel: CardioWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var statsBottomSheetOpen by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.onNavigateBack()
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
                        onClick = viewModel::onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { statsBottomSheetOpen = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timeline),
                            contentDescription = stringResource(id = R.string.stats)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val enabled = uiState.hasChanges
            GymFloatingActionButton(
                enabled = enabled,
                onClick = {
                    if (uiState.hasMarkedMetrics)
                        viewModel.onFinishWorkoutPressed()
                    else
                        viewModel.onSavePressed()
                }
            ) {
                if (uiState.hasMarkedMetrics) {
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
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_large)
                )
        ) {
            if (uiState.loading) {
                CircularProgressIndicator()
            } else {
                val dateString = if (uiState.sessionTimestamp != null) {
                    "${stringResource(id = R.string.adding_for_date)}: ${uiState.sessionTimestamp!!.toDateString()}"
                } else {
                    "${stringResource(id = R.string.last_time)}: ${uiState.previousWorkout?.timestamp?.toDateAndTimeString() ?: "-"}"
                }

                Row(
                    modifier = Modifier
                        .widthIn(dimensionResource(id = R.dimen.breakpoint_small))
                ) {
                    Text(
                        text = dateString
                    )
                }
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                CardioContent(
                    steps = uiState.workout.metrics.steps,
                    onStepsChange = viewModel::onStepsChange,
                    distance = uiState.workout.metrics.distance,
                    onDistanceChange = viewModel::onDistanceChange,
                    onDurationChange = viewModel::onDurationChange,
                    modifier = Modifier.widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
                )
            }
        }
    }

    if (statsBottomSheetOpen && uiState.stats != null) {
        ModalBottomSheet(
            onDismissRequest = { statsBottomSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
        ) {
            CardioWorkoutStatsList(
                stats = uiState.stats!!
            )
        }
    }

    if (uiState.unSavedChangesDialogOpen) {
        UnsavedChangesDialog(
            onConfirm = viewModel::onConfirmUnsavedChangesDialog,
            onCancel = viewModel::dismissUnsavedChangesDialog
        )
    }

    if (uiState.finishWorkoutDialogOpen) {
        FinishWorkoutDialog(
            onCancel = viewModel::dismissFinishWorkoutDialog,
            onFinishWorkout = viewModel::onConfirmFinishWorkoutDialog
        )
    }
}