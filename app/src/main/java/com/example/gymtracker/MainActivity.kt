package com.example.gymtracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.gymtracker.ui.cardio.cardiolist.CardioListScreen
import com.example.gymtracker.ui.navigation.GymScaffold
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.ui.welcome.WelcomeScreen
import com.example.gymtracker.ui.workouts.createsplit.CreateSplitScreen
import com.example.gymtracker.ui.workouts.split.SplitScreen
import com.example.gymtracker.ui.workouts.splitslist.SplitListScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.loading
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            GymTrackerTheme {
                if (!uiState.loading)
                    GymTrackerApp(
                        viewModel = viewModel
                    )
            }
        }
    }
}

@Composable
fun GymTrackerApp(
    viewModel: MainViewModel
) {
    val appUiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navigationAnimationMoveInt = 1500

    GymScaffold(
        navController = navController
    ) { innerPadding ->
        NavHost(
            enterTransition = { slideInHorizontally(initialOffsetX = { navigationAnimationMoveInt }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -navigationAnimationMoveInt }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -navigationAnimationMoveInt }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { navigationAnimationMoveInt }) },
            navController = navController,
            startDestination = appUiState.initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Welcome> {
                WelcomeScreen(
                    onUnderstoodClick = {
                        viewModel.onUserWelcomed()
                        navController.navigate(Route.Workouts)
                    }
                )
            }

            navigation<Route.Workouts>(startDestination = Route.SplitList) {
                composable<Route.SplitList> {
                    SplitListScreen(
                        onNavigateToSplit = { navController.navigate(Route.Split(it)) },
                        onNavigateToCreateSplit = { navController.navigate(Route.CreateSplit) }
                    )
                }
                composable<Route.Split> {
                    SplitScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<Route.CreateSplit> {
                    CreateSplitScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onCreate = { navController.popBackStack() }
                    )
                }
            }

            navigation<Route.Cardio>(startDestination = Route.CardioList) {
                composable<Route.CardioList> {
                    CardioListScreen()
                }
            }

            navigation<Route.Stats>(startDestination = Route.StatsOverview) {
                composable<Route.StatsOverview> {
                    Text("stats")
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "fi", showSystemUi = true)
@Composable
private fun ThemePreview() {
    GymTrackerTheme {
        ThemePreviewColumn()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun ThemePreviewDark() {
    GymTrackerTheme(
        darkTheme = true
    ) {
        ThemePreviewColumn()
    }
}

@Composable
fun ThemePreviewColumn() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlowRow {
                Text("This is headline", style = MaterialTheme.typography.headlineSmall)
                Text("This is body", style = MaterialTheme.typography.bodyLarge)
                Text("This is label", style = MaterialTheme.typography.labelMedium)
            }

            FlowRow {
                Button(onClick = {}) {
                    Text("Button")
                }
                ElevatedButton(onClick = {}) {
                    Text("Elevated")
                }
                OutlinedButton(onClick = {}) {
                    Text("Outlined")
                }
            }

            TextField(value = "Text field", onValueChange = { }, label = { Text("Text Field") })
            OutlinedTextField(
                value = "Outlined text field",
                onValueChange = { },
                label = { Text("Outlined Field") }
            )

            FlowRow {
                Card {
                    Text("Card Content", modifier = Modifier.padding(8.dp))
                }
                OutlinedCard {
                    Text("Outlined card", modifier = Modifier.padding(8.dp))
                }
                ElevatedCard {
                    Text("Elevated Card", modifier = Modifier.padding(8.dp))
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Secondarycontainer",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "tertiarycontainer", modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            "secondary", modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                        Text(
                            "tertiary", modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "⚠️ Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "This is an error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            FlowRow {
                Surface {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 50.dp,
                    shadowElevation = 2.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 100.dp,
                    shadowElevation = 4.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 500.dp,
                    shadowElevation = 6.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    tonalElevation = 1000.dp,
                    shadowElevation = 8.dp
                ) {
                    Text("Surface ", modifier = Modifier.padding(8.dp))
                }
                Surface(
                    checked = true,
                    onCheckedChange = {},
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text("Checked Surface", modifier = Modifier.padding(8.dp))
                }
            }

            FlowRow {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Checkbox(checked = false, onCheckedChange = {})
                Checkbox(checked = true, onCheckedChange = {})
                Switch(checked = false, onCheckedChange = {})
                Switch(checked = true, onCheckedChange = {})
                RadioButton(selected = false, onClick = {})
                RadioButton(selected = true, onClick = {})
            }

            Slider(value = 0.5f, onValueChange = {})

            FlowRow {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "FAB")
                }
                LinearProgressIndicator(
                    progress = { 0.5f },
                )
                CircularProgressIndicator()
            }

            HorizontalDivider()

        }
    }
}
