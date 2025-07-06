package com.example.gymtracker.ui.cardio.cardiolist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.EmptyListCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardioListScreen(viewModel: CardioListViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    CardioListScreen(
        cardios = uiState.cardios
    )
}

@Composable
private fun CardioListScreen(
    cardios: List<String>
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (cardios.isEmpty()) {
            EmptyListCard(
                icon = painterResource(id = R.drawable.run),
                subtitle = stringResource(id = R.string.cardio_intro)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(cardios) {

                }
            }
        }
    }
}

@Composable
private fun Cardio() {
    ElevatedCard() { }
}