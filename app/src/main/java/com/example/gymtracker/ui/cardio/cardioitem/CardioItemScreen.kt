package com.example.gymtracker.ui.cardio.cardioitem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardioItemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getCardio()
    }

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
            viewModel.onFinishPressed { onNavigateBack() }
        },
        visible = true
    ) {
        Icon(
            imageVector = Icons.Default.Done,
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
}