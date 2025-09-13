package com.tonicantarella.gymtracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
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
        ElevatedCard (
            modifier = modifier
        ){
            Column(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                subtitle()
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