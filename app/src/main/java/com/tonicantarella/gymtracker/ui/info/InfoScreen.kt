package com.tonicantarella.gymtracker.ui.info

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.core.net.toUri
import com.tonicantarella.gymtracker.BuildConfig
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.common.GymDialog
import com.tonicantarella.gymtracker.ui.common.GymScaffold
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
                    Text(
                        text = stringResource(id = R.string.info)
                    )
                }
            )
        }
    ) { innerPadding ->
        InfoScreen(
            showFinishWorkoutDialog = uiState.value.confirmFinishWorkout,
            onFinishWorkoutDialogChecked = viewModel::onShowFinishDialogChecked,
            onDeleteAllData = { deletionDialogOpen = true },
            modifier = Modifier.padding(innerPadding)
        )
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
            Text(
                text = "${stringResource(id = R.string.version)}: ${BuildConfig.APP_VERSION}"
            )
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
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
        HorizontalDivider()
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