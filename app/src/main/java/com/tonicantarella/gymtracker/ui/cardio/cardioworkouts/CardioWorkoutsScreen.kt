package com.tonicantarella.gymtracker.ui.cardio.cardioworkouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.EmptyListCard
import com.tonicantarella.gymtracker.ui.common.GymDialog
import com.tonicantarella.gymtracker.ui.common.GymFloatingActionButton
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.common.WorkoutList
import com.tonicantarella.gymtracker.ui.entity.WorkoutWithTimestamp
import com.tonicantarella.gymtracker.utility.MAX_CARDIO
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioWorkoutsScreen(
    viewModel: CardioWorkoutsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getWorkouts()
    }

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.run),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = stringResource(id = R.string.cardio)
                        )
                    }
                },
                actions = {
                    Row {
                        AnimatedVisibility(
                            visible = uiState.selectingItems,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            IconButton(
                                onClick = viewModel::stopSelectingItems
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cancel)
                                )
                            }
                        }
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !uiState.selectingItems && uiState.workouts.isNotEmpty(),
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
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            GymFloatingActionButton(
                onClick = viewModel::onCreateCardioClicked,
                enabled = uiState.workouts.size < MAX_CARDIO
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        }
    ) { innerPadding ->
        CardioWorkoutsScreen(
            loading = uiState.loading,
            workouts = uiState.workouts,
            selectingItems = uiState.selectingItems,
            selectedItems = uiState.selectedItems,
            onSelectWorkout = viewModel::onSelectItem,
            onWorkoutClick = { viewModel.onNavigateToWorkout(it.id) },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (deletionDialogOpen) {
        GymDialog(
            onDismissRequest = { deletionDialogOpen = false },
            title = {},
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
            onCancel = {
                deletionDialogOpen = false
                viewModel.stopSelectingItems()
            }
        )
    }
}

@Composable
private fun CardioWorkoutsScreen(
    loading: Boolean,
    workouts: List<WorkoutWithTimestamp>,
    selectingItems: Boolean,
    selectedItems: List<WorkoutWithTimestamp>,
    onSelectWorkout: (workout: WorkoutWithTimestamp) -> Unit,
    onWorkoutClick: (workout: WorkoutWithTimestamp) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (workouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
            ) {
                EmptyListCard(
                    iconPainter = painterResource(id = R.drawable.run),
                    subtitle = {
                        Text(
                            text = stringResource(id = R.string.cardio_intro),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        } else {
            WorkoutList(
                workouts = workouts,
                selectingItems = selectingItems,
                selectedItems = selectedItems,
                onSelect = onSelectWorkout,
                onClick = onWorkoutClick
            )
        }
    }
}