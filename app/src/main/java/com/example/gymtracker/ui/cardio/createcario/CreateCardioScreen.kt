package com.example.gymtracker.ui.cardio.createcario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.gymtracker.R
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.navigation.TopBarTextField
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

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
            contentDescription = stringResource(id = R.string.save)
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