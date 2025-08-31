package com.example.gymtracker.ui.cardio.cardioitem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    onNavigateToStats: (id: Int) -> Unit,
    viewModel: CardioItemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges =
        uiState.cardio != uiState.initialCardio

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(hasUnsavedChanges) {
        if (hasUnsavedChanges) {
            onNavigationGuardChange(true)
        } else {
            onNavigationGuardChange(false)
        }
    }

    ProvideTopAppBar(
        title = {
            TopBarTextField(
                value = uiState.cardio?.name ?: "",
                onValueChange = viewModel::onChangeName,
                maxSize = CARDIO_NAME_MAX_SIZE
            )
        },
        navigationItem = {
            IconButton(
                onClick = { onNavigateBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            // TODO this should not navigate, but instead show a modal
            IconButton(
                onClick = { onNavigateToStats(uiState.cardioId) }
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
            steps = uiState.cardio?.steps?.value,
            onStepsChange = viewModel::onStepsChange,
            previousSteps = uiState.previousCardio?.steps?.value,
            previousStepsTimestamp = uiState.previousCardio?.steps?.timestamp,
            distance = uiState.cardio?.distance?.value,
            previousDistance = uiState.previousCardio?.distance?.value,
            onDistanceChange = viewModel::onDistanceChange,
            previousDistanceTimestamp = uiState.previousCardio?.distance?.timestamp,
            previousDuration = uiState.previousCardio?.duration?.value,
            previousDurationTimestamp = uiState.previousCardio?.duration?.timestamp,
            onDurationChange = viewModel::onDurationChange
        )
    }
}