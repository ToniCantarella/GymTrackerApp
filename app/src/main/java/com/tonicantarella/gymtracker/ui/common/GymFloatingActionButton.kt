package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun GymFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = {
            if (enabled){
                onClick()
            }
        },
        shape = CircleShape,
        containerColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
        contentColor = if (enabled) Color.White else Color.Black.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        icon()
    }
}