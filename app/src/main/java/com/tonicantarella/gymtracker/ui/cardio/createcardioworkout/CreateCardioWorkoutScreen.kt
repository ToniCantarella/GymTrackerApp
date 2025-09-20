package com.tonicantarella.gymtracker.ui.cardio.createcardioworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.cardio.common.CardioContent
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.TopBarTextField
import com.tonicantarella.gymtracker.ui.common.UnsavedChangesDialog
import com.tonicantarella.gymtracker.utility.CARDIO_NAME_MAX_SIZE
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardioWorkoutScreen(
    viewModel: CreateCardioWorkoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        viewModel.onNavigateBack()
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
                        onClick = viewModel::onNavigateBack
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
                onClick = viewModel::onCreateWorkoutPressed
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
                        Color.Gray.copy(alpha = .4f)
                    )
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_large)
                    )
            ) {
                CardioContent(
                    modifier = Modifier
                        .widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
                )
            }
        }
    }

    if (uiState.unSavedChangesDialogOpen) {
        UnsavedChangesDialog(
            onConfirm = viewModel::onConfirmUnsavedChangesDialog,
            onCancel = viewModel::dismissUnsavedChangesDialog
        )
    }
}