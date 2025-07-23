package com.example.gymtracker.ui.cardio.cardiolist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.gymtracker.ui.common.WorkoutListItem
import com.example.gymtracker.ui.navigation.ProvideFloatingActionButton
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.MAX_CARDIO
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioListScreen(
    onNavigateToCardioItem: (id: Int) -> Unit,
    onNavigateToCreateCardio: () -> Unit,
    viewModel: CardioListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getCardioList()
    }

    ProvideTopAppBar(
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete)
                )
            }
        }
    )

    ProvideFloatingActionButton(
        onClick = onNavigateToCreateCardio,
        visible = uiState.cardioList.size < MAX_CARDIO
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
                        viewModel.onDeleteCardioList { deletionDialogOpen = false }
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

    CardioListScreen(
        loading = uiState.loading,
        cardioList = uiState.cardioList,
        selectingItems = uiState.selectingItems,
        selectedItems = uiState.selectedItems,
        onSelect = viewModel::onSelectItem,
        onCardioClick = onNavigateToCardioItem,
        onCardioHold = viewModel::startSelectingItems
    )
}

@Composable
private fun CardioListScreen(
    loading: Boolean,
    cardioList: List<WorkoutListItem>,
    selectingItems: Boolean,
    selectedItems: List<Int>,
    onSelect: (id: Int) -> Unit,
    onCardioHold: (id: Int) -> Unit,
    onCardioClick: (id: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (cardioList.isEmpty()) {
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                cardioList.forEach { cardio ->
                    WorkoutListItem(
                        workout = cardio,
                        selectingItems = selectingItems,
                        selected = selectedItems.contains(cardio.id),
                        onSelect = onSelect,
                        onHold = { onCardioHold(cardio.id) },
                        onClick = { onCardioClick(cardio.id) }
                    )
                }
            }
        }
    }
}