package com.example.gymtracker.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.gymtracker.database.repository.WorkoutListItem
import kotlinx.coroutines.delay

@Composable
fun WorkoutList(
    workouts: List<WorkoutListItem>,
    selectingItems: Boolean,
    onSelect: (id: Int, selected: Boolean) -> Unit,
    onHold: (id: Int) -> Unit,
    onClick: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(workouts) { index, workout ->
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn()
            ) {
                WorkoutListItem(
                    workout = workout,
                    selectingItems = selectingItems,
                    selected = workout.selected,
                    onSelect = { onSelect(workout.id, it) },
                    onHold = onHold,
                    onClick = onClick
                )
            }
        }
    }
}