package com.example.gymtracker.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
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
    Box {
        Icon(
            painter = painterResource(id = R.drawable.weight),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -150F
                    translationY = 200f
                }
                .blur(5.dp)
                .align(Alignment.BottomStart)
                .size(400.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = .2f)
        )
        Icon(
            painter = painterResource(id = R.drawable.run),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    translationX = 200F
                    translationY = -50f
                }
                .blur(10.dp)
                .align(Alignment.TopEnd)
                .size(300.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = .2f)
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.welcome),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = stringResource(id = R.string.welcome_text),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large)))
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
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    GymTrackerTheme {
        Surface {
            WelcomeScreen(
                onUnderstoodClick = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "fi")
@Composable
private fun WelcomeScreenPreviewFi() {
    GymTrackerTheme(darkTheme = true) {
        Surface{
            WelcomeScreen(
                onUnderstoodClick = {}
            )
        }
    }
}