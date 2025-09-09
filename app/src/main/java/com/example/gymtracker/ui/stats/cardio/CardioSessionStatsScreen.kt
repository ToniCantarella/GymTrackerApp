package com.example.gymtracker.ui.stats.cardio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.entity.cardio.WorkoutWithMetrics
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardioSessionStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardioSessionStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = uiState.cardio?.name ?: ""
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

    CardioSessionStatsScreen(
        loading = uiState.loading,
        cardio = uiState.cardio
    )
}

@Composable
private fun CardioSessionStatsScreen(
    loading: Boolean,
    cardio: WorkoutWithMetrics?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (cardio != null) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .pointerInput(Unit) {}
                )
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                    ) {
                        Text(
                            text = cardio.timestamp?.toDateAndTimeString() ?: "-"
                        )
                    }
                    CardioContent(
                        steps = cardio.metrics.steps,
                        distance = cardio.metrics.distance,
                        displayDuration = cardio.metrics.duration
                    )
                }
            }
        }
    }
}