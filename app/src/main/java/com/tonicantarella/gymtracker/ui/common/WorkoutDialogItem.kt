package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.tonicantarella.gymtracker.R

@Composable
fun WorkoutDialogItem(
    onClick: () -> Unit,
    workoutName: String,
    modifier: Modifier = Modifier,
    timestamp: String? = null,
    icon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                }
                Column {
                    Text(
                        text = workoutName
                    )
                    if (timestamp != null) {
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_small)))
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.history),
                                contentDescription = null
                            )
                            Text(
                                text = timestamp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
}