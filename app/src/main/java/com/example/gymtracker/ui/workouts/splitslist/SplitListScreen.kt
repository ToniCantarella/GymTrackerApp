package com.example.gymtracker.ui.workouts.splitslist

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
import com.example.gymtracker.ui.common.WorkoutListItem
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.MAX_SPLITS
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun SplitListScreen(
    onNavigateToSplit: (id: Int) -> Unit,
    onNavigateToCreateSplit: () -> Unit,
    viewModel: SplitListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getSplits()
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
                    enabled = uiState.splits.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete)
                    )
                }
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = onNavigateToCreateSplit,
        visible = !uiState.selectingItems && uiState.splits.size < MAX_SPLITS
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
                        viewModel.onDeleteSplits { deletionDialogOpen = false }
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

    SplitListScreen(
        loading = uiState.loading,
        splits = uiState.splits,
        selectingItems = uiState.selectingItems,
        selectedItems = uiState.selectedItems,
        onSelect = viewModel::onSelectItem,
        onSplitClick = onNavigateToSplit,
        onSplitHold = viewModel::startSelectingItems
    )
}

@Composable
fun SplitListScreen(
    loading: Boolean,
    splits: List<WorkoutListItem>,
    selectingItems: Boolean,
    selectedItems: List<Int>,
    onSelect: (id: Int) -> Unit,
    onSplitHold: (id: Int) -> Unit,
    onSplitClick: (id: Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (splits.isEmpty()) {
            EmptyListCard(
                icon = painterResource(id = R.drawable.weight),
                subtitle = {
                    Text(
                        text = stringResource(id = R.string.workouts_intro),
                        textAlign = TextAlign.Center
                    )
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                splits.forEach { split ->
                    WorkoutListItem(
                        workout = split,
                        selectingItems = selectingItems,
                        selected = selectedItems.contains(split.id),
                        onSelect = onSelect,
                        onHold = { onSplitHold(split.id) },
                        onClick = { onSplitClick(split.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitsPreview() {
    GymTrackerTheme {
        SplitListScreen(
            loading = false,
            splits = listOf(
                WorkoutListItem(
                    id = 0,
                    name = "Workout 1",
                    latestTimestamp = Instant.now()
                )
            ),
            selectingItems = false,
            onSplitClick = {},
            onSelect = {},
            selectedItems = emptyList(),
            onSplitHold = {}
        )
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun EmptySplitsPreview() {
    GymTrackerTheme {
        SplitListScreen(
            loading = false,
            splits = emptyList(),
            selectingItems = false,
            onSelect = {},
            selectedItems = emptyList(),
            onSplitClick = {},
            onSplitHold = {}
        )
    }
}