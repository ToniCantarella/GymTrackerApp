package com.tonicantarella.gymtracker.ui.cardio.createcardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.cardio.common.CardioContent
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardioWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigationGuardChange: (Boolean) -> Unit,
    releaseNavigationGuard: () -> Unit,
    viewModel: CreateCardioWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasUnsavedChanges = uiState.name.isNotEmpty()

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

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopBarTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onChangeName,
                        maxSize = CARDIO_NAME_MAX_SIZE
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val enabled = uiState.name.isNotEmpty()
            GymFloatingActionButton(
                enabled = enabled,
                onClick = {
                    releaseNavigationGuard()
                    viewModel.onSavePressed { onNavigateBack() }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .pointerInput(Unit) {}
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = .5f)
                    )
            )
            CardioContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateCardioPreview() {
    GymTrackerTheme {
        CardioContent(
            steps = 0,
            distance = 0.0
        )
    }
}