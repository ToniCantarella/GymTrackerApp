package com.tonicantarella.gymtracker.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymDialog
import com.tonicantarella.gymtracker.ui.theme.info
import com.tonicantarella.gymtracker.ui.theme.success
import com.tonicantarella.gymtracker.ui.theme.warning

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    updateStatus: UpdateStatus,
    progress: Float
) {
    when (updateStatus) {
        UpdateStatus.AVAILABLE -> UpdateAvailable(
            onCancel = onDismiss,
            onUpdate = onUpdate
        )

        UpdateStatus.DOWNLOADING -> UpdateDownloading(
            progress = progress
        )

        UpdateStatus.DOWNLOADED -> UpdateDownloaded(
            onDone = onDismiss
        )

        UpdateStatus.CANCELED -> UpdateCancelled(
            onCancel = onDismiss,
            onUpdate = onUpdate
        )

        UpdateStatus.FAILED -> UpdateFailed(
            onCancel = onDismiss,
            onRetry = onUpdate
        )

        UpdateStatus.INSTALLED -> UpdateInstalled(
            onDone = onUpdate
        )

        else -> UpdateUnknown(
            onCancel = onDismiss,
            onRetry = onUpdate
        )
    }
}

@Composable
private fun UpdateAvailable(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.update),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.info,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.update_available)
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.update_available_description)
            )
        },
        onDismissRequest = onCancel,
        onCancel = onCancel,
        confirmButton = {
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.update))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateDownloading(
    progress: Float,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = {},
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.downloading)
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                LinearProgressIndicator(
                    progress = { progress }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateDownloaded(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = onDone,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.info,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.downloaded)
            )
        },
        confirmButton = {
            Button(onClick = onDone) {
                Text(text = stringResource(id = R.string.done))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateCancelled(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.warning),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.warning,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.update_cancelled)
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.update_cancelled_description)
            )
        },
        onCancel = onCancel,
        confirmButton = {
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.update))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateFailed(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.update_failed)
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.update_failed_description)
            )
        },
        onCancel = onCancel,
        confirmButton = {
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.retry))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateInstalled(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = onDone,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.success,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.update_installed)
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.update_installed_description)
            )
        },
        confirmButton = {
            Button(onClick = onDone) {
                Text(text = stringResource(id = R.string.done))
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpdateUnknown(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.question_mark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxSize()
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.update_unknown)
            )
        },
        subtitle = {
            Text(
                text = stringResource(id = R.string.update_unknown_description)
            )
        },
        onCancel = onCancel,
        confirmButton = {
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.retry))
            }
        },
        modifier = modifier
    )
}