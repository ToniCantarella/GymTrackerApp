package com.example.gymtracker.ui.cardio.cardioworkouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.common.EmptyListCard
import com.example.gymtracker.ui.common.WorkoutList
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.MAX_CARDIO
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioWorkoutsScreen(
    onNavigateToWorkout: (id: Int) -> Unit,
    onNavigateToCreateCardio: () -> Unit,
    viewModel: CardioWorkoutsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getWorkouts()
    }

    ProvideTopAppBar(
        actions = {
            if (uiState.selectingItems) {
                IconButton(
                    onClick = viewModel::stopSelectingItems
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close)
                    )
                }
                IconButton(
                    onClick = { deletionDialogOpen = true },
                    enabled = uiState.selectedItems.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = stringResource(id = R.string.delete)
                    )
                }
            } else {
                IconButton(
                    onClick = viewModel::startSelectingItems,
                    enabled = uiState.workouts.isNotEmpty()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.checklist),
                        contentDescription = stringResource(id = R.string.select)
                    )
                }
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = onNavigateToCreateCardio,
        visible = uiState.workouts.size < MAX_CARDIO
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.add)
        )
    }

    if (deletionDialogOpen) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(
                        id = R.string.delete_are_you_sure_multiple_items,
                        uiState.selectedItems.size
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletionDialogOpen = false
                        viewModel.onDeleteWorkouts()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.delete)
                    )
                }
            },
            cancelButton = {
                OutlinedButton(
                    onClick = {
                        deletionDialogOpen = false
                        viewModel.stopSelectingItems()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                    )
                }
            },
            onDismissRequest = { deletionDialogOpen = false }
        )
    }

    CardioWorkoutsScreen(
        loading = uiState.loading,
        workouts = uiState.workouts,
        selectingItems = uiState.selectingItems,
        selectedItems = uiState.selectedItems,
        onSelectWorkout = viewModel::onSelectItem,
        onWorkoutClick = { onNavigateToWorkout(it.id) },
        onWorkoutHold = viewModel::startSelectingItems
    )
}

@Composable
private fun CardioWorkoutsScreen(
    loading: Boolean,
    workouts: List<WorkoutWithTimestamp>,
    selectingItems: Boolean,
    selectedItems: List<WorkoutWithTimestamp>,
    onSelectWorkout: (workout: WorkoutWithTimestamp) -> Unit,
    onWorkoutClick: (workout: WorkoutWithTimestamp) -> Unit,
    onWorkoutHold: (workout: WorkoutWithTimestamp) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (workouts.isEmpty()) {
            EmptyListCard(
                iconPainter = painterResource(id = R.drawable.run),
                subtitle = {
                    Text(
                        text = stringResource(id = R.string.cardio_intro),
                        textAlign = TextAlign.Center
                    )
                }
            )
        } else {
            WorkoutList(
                workouts = workouts,
                selectingItems = selectingItems,
                selectedItems = selectedItems,
                onSelect = onSelectWorkout,
                onHold = onWorkoutHold,
                onClick = onWorkoutClick
            )
        }
    }
}