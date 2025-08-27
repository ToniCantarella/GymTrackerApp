package com.example.gymtracker.ui.stats.cardio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.gymtracker.ui.cardio.entity.Cardio
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CardioSessionScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardioSessionViewModel = koinViewModel()
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

    CardioSessionScreen(
        loading = uiState.loading,
        cardio = uiState.cardio
    )
}

@Composable
private fun CardioSessionScreen(
    loading: Boolean,
    cardio: Cardio?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                            text = cardio.latestTimestamp?.toDateAndTimeString() ?: "-"
                        )
                    }
                    CardioContent(
                        steps = cardio.steps,
                        distance = cardio.distance,
                        displayDuration = cardio.duration
                    )
                }
            }
        }
    }
}