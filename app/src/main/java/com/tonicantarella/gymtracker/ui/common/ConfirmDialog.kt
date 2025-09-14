package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.tonicantarella.gymtracker.R

@Composable
fun ConfirmDialog(
    subtitle: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    cancelButton: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_extra_large))
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                ) {
                    subtitle()
                }
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    cancelButton()
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                    confirmButton()
                }
            }
        }
    }
}