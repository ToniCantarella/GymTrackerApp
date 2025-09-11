package com.example.gymtracker.ui.gym.gymworkouts

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import com.example.gymtracker.ui.common.EmptyListCard
import com.example.gymtracker.ui.common.WorkoutList
import com.example.gymtracker.ui.entity.WorkoutWithTimestamp
import com.example.gymtracker.ui.theme.GymTrackerTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymWorkoutsScreen(
    onNavigateToWorkout: (id: Int) -> Unit,
    onCreateWorkoutClicked: () -> Unit,
    viewModel: GymWorkoutsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getWorkouts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.gym_workouts)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = uiState.selectingItems,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(
                                enabled = uiState.selectedItems.isNotEmpty(),
                                onClick = { deletionDialogOpen = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                    disabledContentColor = MaterialTheme.colorScheme.error.copy(
                                        alpha = .5f
                                    )
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete)
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !uiState.selectingItems,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(
                                onClick = viewModel::startSelectingItems
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.checklist),
                                    contentDescription = stringResource(R.string.select),
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateWorkoutClicked
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        }
    ) { innerPadding ->
        GymWorkoutsScreen(
            loading = uiState.loading,
            workouts = uiState.workouts,
            selectingItems = uiState.selectingItems,
            selectedItems = uiState.selectedItems,
            onSelectWorkout = viewModel::onSelectItem,
            onWorkoutClick = { onNavigateToWorkout(it.id) },
            onWorkoutHold = viewModel::startSelectingItems,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (deletionDialogOpen) {
        val dialogSubtitle = if (uiState.selectedItems.size > 1) {
            stringResource(
                id = R.string.delete_are_you_sure_multiple_items,
                uiState.selectedItems.size
            )
        } else {
            stringResource(
                id = R.string.delete_are_you_sure_singular_item,
                uiState.selectedItems.first().name
            )
        }

        ConfirmDialog(
            subtitle = {
                Text(
                    text = dialogSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletionDialogOpen = false
                        viewModel.onDeleteWorkoutPlans()
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
}

@Composable
fun GymWorkoutsScreen(
    loading: Boolean,
    workouts: List<WorkoutWithTimestamp>,
    selectingItems: Boolean,
    selectedItems: List<WorkoutWithTimestamp>,
    onSelectWorkout: (workout: WorkoutWithTimestamp) -> Unit,
    onWorkoutClick: (workout: WorkoutWithTimestamp) -> Unit,
    onWorkoutHold: (workout: WorkoutWithTimestamp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (workouts.isEmpty()) {
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

@Preview(showBackground = true)
@Composable
private fun WorkoutPlansPreview() {
    GymTrackerTheme {
        Surface {
            GymWorkoutsScreen(
                loading = false,
                workouts = listOf(
                    WorkoutWithTimestamp(
                        id = 0,
                        name = "Workout 1",
                        timestamp = Instant.now()
                    )
                ),
                selectedItems = emptyList(),
                selectingItems = false,
                onWorkoutClick = {},
                onSelectWorkout = {},
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
            GymWorkoutsScreen(
                loading = false,
                workouts = emptyList(),
                selectedItems = emptyList(),
                selectingItems = false,
                onSelectWorkout = {},
                onWorkoutClick = {},
                onWorkoutHold = {}
            )
        }
    }
}