package com.example.gymtracker.ui.common

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.R

@Composable
fun UnsavedChangesDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier) {
    ConfirmDialog(
        subtitle = {
            Text(
                text = stringResource(id = R.string.unsaved_changes),
                textAlign = TextAlign.Center
            )
        },
        cancelButton = {
            OutlinedButton(
                onClick = { onCancel() }
            ) {
                Text(
                    text = stringResource(id = R.string.cancel)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() }
            ) {
                Text(
                    text = stringResource(id = R.string.ok)
                )
            }
        },
        onDismissRequest = { onCancel() },
        modifier = modifier
    )
}