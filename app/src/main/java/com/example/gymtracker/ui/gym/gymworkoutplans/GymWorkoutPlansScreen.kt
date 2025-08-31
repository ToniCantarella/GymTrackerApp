package com.example.gymtracker.ui.gym.gymworkoutplans

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.common.EmptyListCard
import com.example.gymtracker.ui.common.WorkoutList
import com.example.gymtracker.ui.entity.WorkoutWithLatestTimestamp
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.MAX_GYM_WORKOUTS
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun GymWorkoutPlansScreen(
    onNavigateToWorkout: (id: Int) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    viewModel: GymWorkoutPlansViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedItemsCount by remember(uiState.workoutPlans) {
        derivedStateOf { uiState.workoutPlans.count { it.selected } }
    }
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getWorkoutPlans()
    }

    ProvideTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.gym_workouts)
            )
        },
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
                    enabled = selectedItemsCount > 0
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
                    enabled = uiState.workoutPlans.isNotEmpty()
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
        onClick = onNavigateToCreateWorkout,
        visible = !uiState.selectingItems && uiState.workoutPlans.size < MAX_GYM_WORKOUTS
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
                        id = R.string.delete_are_you_sure,
                        selectedItemsCount
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteWorkoutPlans { deletionDialogOpen = false }
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

    GymWorkoutPlansScreen(
        loading = uiState.loading,
        workoutPlans = uiState.workoutPlans,
        selectingItems = uiState.selectingItems,
        onSelect = viewModel::onSelectItem,
        onWorkoutClick = onNavigateToWorkout,
        onWorkoutHold = viewModel::startSelectingItems
    )
}

@Composable
fun GymWorkoutPlansScreen(
    loading: Boolean,
    workoutPlans: List<WorkoutWithLatestTimestamp>,
    selectingItems: Boolean,
    onSelect: (id: Int, selected: Boolean) -> Unit,
    onWorkoutClick: (id: Int) -> Unit,
    onWorkoutHold: (id: Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (workoutPlans.isEmpty()) {
            EmptyListCard(
                iconPainter = painterResource(id = R.drawable.weight),
                subtitle = {
                    Text(
                        text = stringResource(id = R.string.gym_workouts_intro),
                        textAlign = TextAlign.Center
                    )
                }
            )
        } else {
            WorkoutList(
                workouts = workoutPlans,
                selectingItems = selectingItems,
                onSelect = onSelect,
                onHold = onWorkoutHold,
                onClick = onWorkoutClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlansPreview() {
    GymTrackerTheme {
        Surface {
            GymWorkoutPlansScreen(
                loading = false,
                workoutPlans = listOf(
                    WorkoutWithLatestTimestamp(
                        id = 0,
                        name = "Workout 1",
                        latestTimestamp = Instant.now()
                    )
                ),
                selectingItems = false,
                onWorkoutClick = {},
                onSelect = { _, _ -> },
                onWorkoutHold = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun EmptyListPreview() {
    GymTrackerTheme(darkTheme = true) {
        Surface {
            GymWorkoutPlansScreen(
                loading = false,
                workoutPlans = emptyList(),
                selectingItems = false,
                onSelect = { _, _ -> },
                onWorkoutClick = {},
                onWorkoutHold = {}
            )
        }
    }
}