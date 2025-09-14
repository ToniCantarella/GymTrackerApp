package com.tonicantarella.gymtracker.ui.common

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tonicantarella.gymtracker.R
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme

@Composable
fun GymDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable (() -> Unit) = {},
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    extraActions: @Composable () -> Unit = {},
    icon: @Composable (() -> Unit)? = null
) {
    GymDialog(
        onDismissRequest = onDismissRequest,
        icon = icon,
        title = title,
        subtitle = subtitle,
        confirmButton = {
            if (onConfirm != null) {
                Button(
                    onClick = onConfirm
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            }
        },
        onCancel = onCancel,
        extraActions = extraActions,
        modifier = modifier
    )
}

@Composable
fun GymDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable (() -> Unit) = {},
    icon: @Composable (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    extraActions: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.widthIn(max = dimensionResource(id = R.dimen.breakpoint_compact))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_extra_large))
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        icon()
                    }
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                }
                ProvideTextStyle(
                    MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center)
                ) {
                    title()
                }
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                ) {
                    subtitle()
                }
                if (extraActions != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProvideTextStyle(
                            MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                        ) {
                            extraActions()
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_extra_large)))
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onCancel != null) {
                        TextButton(
                            onClick = onCancel
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel)
                            )
                        }
                        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
                    }
                    confirmButton()
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialogPreview() {
    GymTrackerTheme {
        GymDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            },
            title = {
                Text(
                    text = "This is a title!"
                )
            },
            subtitle = {
                Text(
                    text = "This is a subtitle!"
                )
            },
            onCancel = {},
            confirmButton = {
                Button(
                    onClick = {}
                ) {
                    Text(
                        text = "Confirm"
                    )
                }
            },
            extraActions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = {}
                    )
                    Text(
                        text = "This is an extra action!"
                    )
                }
            }
        )
    }
}