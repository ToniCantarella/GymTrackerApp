package com.example.gymtracker.ui.stats.gym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gymtracker.R
import com.example.gymtracker.ui.entity.gym.WorkoutWithExercises
import com.example.gymtracker.ui.gym.common.ExerciseListView
import com.example.gymtracker.ui.navigation.ProvideTopAppBar
import com.example.gymtracker.utility.toDateAndTimeString
import org.koin.androidx.compose.koinViewModel

@Composable
fun GymSessionScreen(
    onNavigateBack: () -> Unit,
    viewModel: GymSessionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProvideTopAppBar(
        title = {
            Text(
                text = uiState.split?.name ?: ""
            )
        },
        navigationItem = {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }
    )

    GymSessionScreen(
        loading = uiState.loading,
        split = uiState.split
    )
}

@Composable
fun GymSessionScreen(
    loading: Boolean,
    split: WorkoutWithExercises?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (split != null && split.exercises.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
            ) {
                Text(
                    text = split.timestamp?.toDateAndTimeString() ?: "-"
                )
            }
            ExerciseListView(
                exercises = split.exercises
            )
        }
    }
}