package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R

@Composable
fun FinishWorkoutDialog(
    onFinishWorkout: (doNotAskAgain: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var doNotAskAgain by remember { mutableStateOf(false) }

    GymDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.goal),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = "${stringResource(id = R.string.done)}?"
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.confirm_finish_workout)
            )
        },
        onCancel = onCancel,
        onConfirm = { onFinishWorkout(doNotAskAgain) },
        extraActions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    doNotAskAgain,
                    onCheckedChange = {doNotAskAgain = it}
                )
                Text(
                    text = stringResource(id = R.string.do_not_ask_again)
                )
            }
        },
        modifier = modifier
    )
}