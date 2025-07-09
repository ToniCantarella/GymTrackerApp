package com.example.gymtracker.ui.workouts.splitslist

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.EmptyListCard
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun SplitListScreen(
    onSplitNavigate: (id: Int) -> Unit,
    onNavigateToAddSplit: () -> Unit,
    viewModel: SplitListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmDeletionDialogOpen by remember { mutableStateOf(false) }

    ProvideTopAppBar(
        actions = {
            if (uiState.selectingItemsToDelete) {
                IconButton(
                    onClick = viewModel::stopSelectingForDeletion
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = { confirmDeletionDialogOpen = true },
                    enabled = uiState.itemsToDelete.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null
                    )
                }
            } else {
                IconButton(
                    onClick = viewModel::startSelectingForDeletion,
                    enabled = uiState.splits.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = onNavigateToAddSplit,
        visible = !uiState.selectingItemsToDelete
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
    }

    if (confirmDeletionDialogOpen) {
        Dialog(
            onDismissRequest = { confirmDeletionDialogOpen = false }
        ) {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.delete_are_you_sure,
                            uiState.itemsToDelete.size
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { confirmDeletionDialogOpen = false }
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel)
                            )
                        }
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                        Button(
                            onClick = viewModel::onDeleteSelected
                        ) {
                            Text(
                                text = stringResource(id = R.string.delete)
                            )
                        }
                    }
                }
            }
        }
    }

    SplitListScreen(
        splits = uiState.splits,
        selectingItemsToDelete = uiState.selectingItemsToDelete,
        selectedItemsForDeletion = uiState.itemsToDelete,
        onSelectForDeletion = viewModel::onSelectForDeletion,
        onSplitClick = onSplitNavigate,
        onSplitHold = viewModel::startSelectingForDeletion
    )
}

@Composable
fun SplitListScreen(
    splits: List<SplitListItem>,
    selectingItemsToDelete: Boolean,
    selectedItemsForDeletion: List<Int>,
    onSelectForDeletion: (id: Int) -> Unit,
    onSplitHold: () -> Unit,
    onSplitClick: (id: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (splits.isEmpty()) {
            EmptyListCard(
                icon = painterResource(id = R.drawable.weight),
                subtitle = stringResource(id = R.string.workouts_intro)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(splits) { split ->
                    ExerciseListItem(
                        exercise = split,
                        selectingItemsToDelete = selectingItemsToDelete,
                        isSetForDeletion = selectedItemsForDeletion.contains(split.id),
                        onSelectForDeletion = onSelectForDeletion,
                        onHold = onSplitHold,
                        onClick = { onSplitClick(split.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: SplitListItem,
    selectingItemsToDelete: Boolean,
    isSetForDeletion: Boolean,
    onSelectForDeletion: (id: Int) -> Unit,
    onHold: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!selectingItemsToDelete) onClick() },
                onLongClick = onHold
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${stringResource(id = R.string.last_time)}: ${exercise.latestTimestamp.toDateAndTimeString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectingItemsToDelete) {
                Checkbox(
                    checked = isSetForDeletion,
                    onCheckedChange = {
                        onSelectForDeletion(exercise.id)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
private fun SplitsPreview() {
    GymTrackerTheme {
        SplitListScreen(
            splits = listOf(
                SplitListItem(
                    id = 0,
                    name = "Workout 1",
                    latestTimestamp = Instant.now()
                )
            ),
            selectingItemsToDelete = false,
            onSplitClick = {},
            onSelectForDeletion = {},
            selectedItemsForDeletion = emptyList(),
            onSplitHold = {}
        )
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun EmptySplitsPreview() {
    GymTrackerTheme {
        SplitListScreen(
            splits = emptyList(),
            selectingItemsToDelete = false,
            onSelectForDeletion = {},
            selectedItemsForDeletion = emptyList(),
            onSplitClick = {},
            onSplitHold = {}
        )
    }
}