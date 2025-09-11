package com.example.gymtracker.ui.cardio.createcardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.gymtracker.ui.cardio.common.CardioContent
import com.example.gymtracker.ui.theme.GymTrackerTheme
import org.koin.androidx.compose.koinViewModel

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

    Box {
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