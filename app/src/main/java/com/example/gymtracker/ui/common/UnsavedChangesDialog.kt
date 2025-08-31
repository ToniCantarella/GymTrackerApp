package com.example.gymtracker.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R

@Composable
fun UnsavedChangesDialog(
    onConfirm: (doNotAskAgain: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var doNotAskAgain by remember { mutableStateOf(false) }

    ConfirmDialog(
        subtitle = {
            Column {
                Text(
                    text = stringResource(id = R.string.unsaved_changes),
                    textAlign = TextAlign.Center
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        doNotAskAgain,
                        onCheckedChange = { doNotAskAgain = it }
                    )
                    Text(
                        text = stringResource(id = R.string.do_not_ask_again)
                    )
                }
            }
        },
        cancelButton = {
            OutlinedButton(
                onClick = onCancel
            ) {
                Text(
                    text = stringResource(id = R.string.cancel)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(doNotAskAgain) }
            ) {
                Text(
                    text = stringResource(id = R.string.ok)
                )
            }
        },
        onDismissRequest = onCancel,
        modifier = modifier
    )
}