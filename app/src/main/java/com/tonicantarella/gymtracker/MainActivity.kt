package com.tonicantarella.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.update.InAppUpdateHandler
import com.tonicantarella.gymtracker.update.UpdateStatus
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    private val inAppUpdateHandler: InAppUpdateHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.loading
        }

        inAppUpdateHandler.registerUpdateLauncher(this)
        inAppUpdateHandler.checkForUpdate()

        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val updateState by inAppUpdateHandler.uiState.collectAsState()

            GymTrackerTheme {
                if (!uiState.loading) {
                    GymTrackerApp(
                        viewModel = viewModel
                    )
                }
                if (updateState.updateStatus != UpdateStatus.IDLE) {
                    UpdateDialog(
                        onDismiss = inAppUpdateHandler::dismissUpdate,
                        onUpdate = inAppUpdateHandler::startUpdate,
                        updateStatus = updateState.updateStatus,
                        progress = updateState.progress
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateHandler.resumeUpdateIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateHandler.disposeFlexibleUpdate()
    }
}

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    updateStatus: UpdateStatus,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = modifier
        ) {
            when (updateStatus) {
                UpdateStatus.AVAILABLE -> UpdateAvailable(
                    onCancel = onDismiss,
                    onUpdate = onUpdate
                )

                UpdateStatus.DOWNLOADING -> UpdateDownLoading(
                    progress = progress
                )

                UpdateStatus.DOWNLOADED -> UpdateDownLoaded(
                    onDone = onDismiss
                )

                UpdateStatus.CANCELED -> UpdateCancelled(
                    onCancel = onDismiss,
                    onUpdate = onUpdate
                )

                UpdateStatus.FAILED -> UpdateFailed(
                    onCancel = onDismiss,
                    onUpdate = onUpdate
                )

                UpdateStatus.INSTALLED -> UpdateInstalled(
                    onDone = onUpdate
                )

                else -> UpdateUnknown(
                    onCancel = onDismiss,
                    onUpdate = onUpdate
                )
            }
        }
    }
}

@Composable
fun UpdateAvailable(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "Update available")
        Row {
            Button(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun UpdateDownLoading(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "DownLoading")
        LinearProgressIndicator(
            progress = { progress }
        )
    }
}

@Composable
fun UpdateDownLoaded(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "DownLoaded!")
        Row {
            Button(onClick = onDone) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun UpdateCancelled(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "You cancelled the update")
        Row {
            Button(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun UpdateFailed(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "Update failed")
        Row {
            Button(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun UpdateInstalled(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "Update Installed")
        Row {

            Button(onClick = onDone) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun UpdateUnknown(
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = "Unknown")
        Row {
            Button(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = onUpdate) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateDialogPreview() {
    GymTrackerTheme {
        Surface {
            Column {
                UpdateAvailable(
                    onCancel = {},
                    onUpdate = {}
                )
                UpdateDownLoading(
                    progress = .5f
                )
                UpdateDownLoaded(
                    onDone = {}
                )
                UpdateCancelled(
                    onCancel = {},
                    onUpdate = {}
                )
                UpdateFailed(
                    onCancel = {},
                    onUpdate = {}
                )
                UpdateInstalled(
                    onDone = {}
                )
                UpdateUnknown(
                    onCancel = {},
                    onUpdate = {}
                )
            }
        }
    }
}