package com.example.gymtracker.ui.workouts.splitslist

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant

private val splitsTestData: List<ExerciseListItem> = List(40) {
    ExerciseListItem(
        id = it.toLong(),
        name = "Treeni $it",
        lastDate = Instant.now().minus(Duration.ofDays(it.toLong()))
    )
}

data class ExerciseListItem(
    val id: Long,
    val name: String,
    val lastDate: Instant
)

@Composable
fun SplitListScreen(
    onSplitNavigate: (name: String) -> Unit,
    onNavigateToAddSplit: () -> Unit,
    viewModel: SplitListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            } else {
                IconButton(
                    onClick = viewModel::startSelectingForDeletion
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
        onClick = onNavigateToAddSplit
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
    }

    SplitListScreen(
        splits = splitsTestData,
        selectingItemsToDelete = uiState.selectingItemsToDelete,
        selectedItemsForDeletion = uiState.itemsToDelete,
        onSelectForDeletion = viewModel::onSelectForDeletion,
        onSplitClick = onSplitNavigate,
        onSplitHold = viewModel::startSelectingForDeletion
    )
}

@Composable
fun SplitListScreen(
    splits: List<ExerciseListItem>,
    selectingItemsToDelete: Boolean,
    selectedItemsForDeletion: List<Long>,
    onSelectForDeletion: (id: Long) -> Unit,
    onSplitHold: () -> Unit,
    onSplitClick: (name: String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (splits.isEmpty()) {
            EmptySplits()
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
                        onClick = { onSplitClick(split.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: ExerciseListItem,
    selectingItemsToDelete: Boolean,
    isSetForDeletion: Boolean,
    onSelectForDeletion: (id: Long) -> Unit,
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
                    text = "${stringResource(id = R.string.last_time)}: ${exercise.lastDate.toDateAndTimeString()}",
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

@Composable
fun EmptySplits() {
    ElevatedCard(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.weight),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = stringResource(id = R.string.workouts_intro),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitsPreview() {
    GymTrackerTheme {
        SplitListScreen(
            splits = splitsTestData,
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