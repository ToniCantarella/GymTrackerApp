package com.example.gymtracker.ui.workouts.splitslist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymtracker.R
import com.example.gymtracker.ui.theme.GymTrackerTheme

private val splitsTestData = listOf(
    "Push",
    "Pull",
    "Legs"
)

@Composable
fun SplitsScreen() {


    SplitsScreen(
        splits = splitsTestData
    )
}

@Composable
fun SplitsScreen(
    splits: List<String>
) {
    Column (modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(splits) { split ->
                Split(
                    split = split,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun Split(
    split: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(
                text = split
            )
        }
    }
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
private fun SplitsPreview() {
    GymTrackerTheme {
        SplitsScreen(
            splits = splitsTestData
        )
    }
}