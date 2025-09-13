package com.tonicantarella.gymtracker.update

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
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
    Dialog(
        onDismissRequest = onDismiss
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
}

@Composable
private fun DialogContent(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier
            .widthIn(max = 400.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_extra_large))
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        icon()
                    }
                }
                ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                    title()
                }
                if (subtitle != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth(.8f)
                    ) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center
                            )
                        ) {
                            subtitle()
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_extra_large)))
            actions()
        }
    }
}

@Composable
private fun DialogActions(
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier
    ) {
        actions()
    }
}

@Composable
private fun UpdateAvailable(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogContent(
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
        actions = {
            DialogActions {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Button(onClick = onUpdate) {
                    Text(text = stringResource(id = R.string.update))
                }
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
    DialogContent(
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
    DialogContent(
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
        actions = {
            DialogActions {
                Button(onClick = onDone) {
                    Text(text = stringResource(id = R.string.done))
                }
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
    DialogContent(
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
        actions = {
            DialogActions {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Button(onClick = onUpdate) {
                    Text(text = stringResource(id = R.string.update))
                }
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
    DialogContent(
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
        actions = {
            DialogActions {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Button(onClick = onRetry) {
                    Text(text = stringResource(id = R.string.retry))
                }
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
    DialogContent(
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
        actions = {
            DialogActions {
                Button(onClick = onDone) {
                    Text(text = stringResource(id = R.string.done))
                }
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
    DialogContent(
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
        actions = {
            DialogActions {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Button(onClick = onRetry) {
                    Text(text = stringResource(id = R.string.retry))
                }
            }

        },
        modifier = modifier
    )
}

@Preview(
    device = "spec:width=3000px,height=3000px,dpi=440"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "fi",
    device = "spec:width=3000px,height=3000px,dpi=440"
)
@Composable
private fun UpdateDialogPreview() {
    GymTrackerTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
            ) {
                UpdateAvailable(
                    onCancel = {},
                    onUpdate = {}
                )
                UpdateDownloading(
                    progress = .5f
                )
                UpdateDownloaded(
                    onDone = {}
                )
                UpdateCancelled(
                    onCancel = {},
                    onUpdate = {}
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
            ) {
                UpdateFailed(
                    onCancel = {},
                    onRetry = {}
                )
                UpdateInstalled(
                    onDone = {}
                )
                UpdateUnknown(
                    onCancel = {},
                    onRetry = {}
                )
            }
        }
    }
}