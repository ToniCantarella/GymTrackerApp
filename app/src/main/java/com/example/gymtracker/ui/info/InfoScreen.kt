package com.example.gymtracker.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.gymtracker.BuildConfig
import com.example.gymtracker.R
import com.example.gymtracker.ui.common.ConfirmDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun InfoScreen(
    onDeleteFinished: () -> Unit,
    viewModel: InfoViewModel = koinViewModel()
) {
    var deletionDialogOpen by remember { mutableStateOf(false) }

    if (deletionDialogOpen) {
        ConfirmDialog(
            subtitle = {
                Text(
                    text = stringResource(id = R.string.delete_all_data_confirmation_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteAllData { onDeleteFinished() }
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

    InfoScreen(
        onDeleteAllData = { deletionDialogOpen = true }
    )
}

@Composable
private fun InfoScreen(
    onDeleteAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        Text(
            text = "${stringResource(id = R.string.version)}: ${BuildConfig.APP_VERSION}"
        )
        Text(
            text = "${stringResource(id = R.string.calendar_library)}: https://github.com/kizitonwose/Calendar"
        )
        Text(
            text = "${stringResource(id = R.string.chart_library)}: https://ehsannarmani.github.io/ComposeCharts"
        )
        Button(
            onClick = onDeleteAllData,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = stringResource(id = R.string.delete_all_data)
            )
        }
    }
}