package com.example.gymtracker.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gymtracker.R
import com.example.gymtracker.ui.theme.GymTrackerTheme

@Composable
fun WelcomeScreen(
    onUnderstoodClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_large))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.welcome),
                style = MaterialTheme.typography.titleLarge
            )
            Icon(
                painter = painterResource(id = R.drawable.weight),
                contentDescription = stringResource(id = R.string.weight_icon),
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = stringResource(id = R.string.welcome_text),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onUnderstoodClick
            ) {
                Text(
                    text = stringResource(id = R.string.understood)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    GymTrackerTheme {
        WelcomeScreen(
            onUnderstoodClick = {}
        )
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun WelcomeScreenPreviewFi() {
    GymTrackerTheme {
        WelcomeScreen(
            onUnderstoodClick = {}
        )
    }
}