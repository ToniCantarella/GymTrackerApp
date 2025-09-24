package com.tonicantarella.gymtracker.ui.info

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.tonicantarella.gymtracker.BuildConfig
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymDialog
import com.tonicantarella.gymtracker.ui.common.GymScaffold
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    viewModel: InfoViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    var deletionDialogOpen by remember { mutableStateOf(false) }

    GymScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = stringResource(id = R.string.info)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            InfoScreen(
                showFinishWorkoutDialog = uiState.value.confirmFinishWorkout,
                onFinishWorkoutDialogChecked = viewModel::onShowFinishDialogChecked,
                showUnsavedChangesDialog = uiState.value.confirmUnsavedChanges,
                onUnsavedChangesDialogChecked = viewModel::onShowUnsavedChangesDialogChecked,
                onDeleteAllData = { deletionDialogOpen = true },
                modifier = Modifier
                    .widthIn(max = dimensionResource(id = R.dimen.breakpoint_small))
            )
        }
    }

    if (deletionDialogOpen) {
        GymDialog(
            onDismissRequest = { deletionDialogOpen = false },
            title = {},
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
                    onClick = viewModel::onDeleteAllData
                ) {
                    Text(
                        text = stringResource(id = R.string.delete)
                    )
                }
            },
            onCancel = { deletionDialogOpen = false }
        )
    }
}

@Composable
private fun InfoScreen(
    onDeleteAllData: () -> Unit,
    showFinishWorkoutDialog: Boolean,
    onFinishWorkoutDialogChecked: (Boolean) -> Unit,
    showUnsavedChangesDialog: Boolean,
    onUnsavedChangesDialogChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_large))
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
                Text(
                    text = stringResource(id = R.string.settings)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.show_finish_workout_dialog)
                )
                Switch(
                    checked = showFinishWorkoutDialog,
                    onCheckedChange = onFinishWorkoutDialogChecked
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.show_unsaved_changes_dialog)
                )
                Switch(
                    checked = showUnsavedChangesDialog,
                    onCheckedChange = onUnsavedChangesDialogChecked
                )
            }
            Button(
                onClick = onDeleteAllData,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_all_data)
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = stringResource(id = R.string.delete_all_data)
                )
            }
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_small)))
        HorizontalDivider()
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_small)))
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(id = R.string.info)
                )
                Text(
                    text = stringResource(id = R.string.info)
                )
            }
            Text(
                text = "${stringResource(id = R.string.version)}: ${BuildConfig.APP_VERSION}"
            )
            Column {
                Text(
                    text = "${stringResource(id = R.string.developer_website)}: "
                )
                LinkedText(
                    url = "https://www.tonicantarella.com",
                    onClick = ::openLink
                )
            }
            Column {
                Text(
                    text = "${stringResource(id = R.string.calendar_library)}: "
                )
                LinkedText(
                    url = "https://github.com/kizitonwose/Calendar",
                    onClick = ::openLink
                )
            }
            Column {
                Text(
                    text = "${stringResource(id = R.string.chart_library)}: "
                )
                LinkedText(
                    url = "https://github.com/ehsannarmani/ComposeCharts",
                    onClick = ::openLink
                )
            }
        }
    }
}

@Composable
private fun LinkedText(
    url: String,
    onClick: (String) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(url)
        }
        pop()
    }

    BasicText(
        text = annotatedString,
        modifier = Modifier.clickable { onClick(url) }
    )
}

@Preview(
    showBackground = true
)
@Preview(
    showBackground = true, locale = "fi",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=673dp,height=841dp"
)
@Composable
private fun InfoPreview() {
    GymTrackerTheme {
        Surface {
            InfoScreen(
                onDeleteAllData = {},
                showFinishWorkoutDialog = true,
                onFinishWorkoutDialogChecked = {},
                showUnsavedChangesDialog = true,
                onUnsavedChangesDialogChecked = {}
            )
        }
    }
}