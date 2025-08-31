package com.example.gymtracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.gymtracker.ui.cardio.cardioitem.CardioItemScreen
import com.example.gymtracker.ui.cardio.cardiolist.CardioListScreen
import com.example.gymtracker.ui.cardio.createcardio.CreateCardioScreen
import com.example.gymtracker.ui.gym.creategymworkout.CreateGymWorkoutScreen
import com.example.gymtracker.ui.gym.gymworkout.GymWorkoutScreen
import com.example.gymtracker.ui.gym.gymworkouts.GymWorkoutsScreen
import com.example.gymtracker.ui.info.InfoScreen
import com.example.gymtracker.ui.navigation.GymScaffold
import com.example.gymtracker.ui.navigation.Route
import com.example.gymtracker.ui.stats.cardio.CardioSessionScreen
import com.example.gymtracker.ui.stats.cardio.CardioStatsScreen
import com.example.gymtracker.ui.stats.gym.GymSessionStatsScreen
import com.example.gymtracker.ui.stats.gym.SplitStatsScreen
import com.example.gymtracker.ui.stats.overview.StatsOverviewScreen
import com.example.gymtracker.ui.theme.GymTrackerTheme
import com.example.gymtracker.ui.welcome.WelcomeScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.loading
        }

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
fun keyboardAsState(): State<Boolean> {
    val ime = WindowInsets.ime
    val isOpen = ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isOpen)
}

@Composable
fun GymTrackerApp(
    viewModel: MainViewModel
) {
    val mainUiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    val navigationAnimationMoveInt = 1500

    val enter = if (isLandscape) fadeIn() else slideInHorizontally { navigationAnimationMoveInt }
    val exit = if (isLandscape) fadeOut() else slideOutHorizontally { -navigationAnimationMoveInt }
    val popEnter =
        if (isLandscape) fadeIn() else slideInHorizontally { -navigationAnimationMoveInt }
    val popExit =
        if (isLandscape) fadeOut() else slideOutHorizontally { navigationAnimationMoveInt }

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }

    var isNavigationGuarded by remember { mutableStateOf(false) }
    var showNavigationGuard by remember { mutableStateOf(false) }
    var pendingNavigationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun onNavigationGuardChange(guard: Boolean) {
        isNavigationGuarded = guard
    }

    fun releaseNavigationGuard() {
        pendingNavigationAction?.invoke()
        pendingNavigationAction = null
        showNavigationGuard = false
        isNavigationGuarded = false
    }

    fun navigateGuarded(navigationAction: () -> Unit) {
        if (isNavigationGuarded) {
            pendingNavigationAction = navigationAction
            showNavigationGuard = true
        } else {
            navigationAction()
            pendingNavigationAction = null
            showNavigationGuard = false
        }
    }

    fun navigate(route: Route) {
        navigateGuarded {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    fun popBackStack() {
        navigateGuarded {
            navController.popBackStack()
        }
    }

    GymScaffold(
        navController = navController,
        navigate = ::navigate
    ) { innerPadding ->
        NavHost(
            enterTransition = { enter },
            exitTransition = { exit },
            popEnterTransition = { popEnter },
            popExitTransition = { popExit },
            navController = navController,
            startDestination = mainUiState.initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Welcome> {
                WelcomeScreen(
                    onUnderstoodClick = {
                        viewModel.onUserWelcomed()
                        navigate(Route.Gym)
                    }
                )
            }

            navigation<Route.Gym>(startDestination = Route.GymWorkoutPlans) {
                composable<Route.GymWorkoutPlans> {
                    GymWorkoutsScreen(
                        onNavigateToWorkout = { navigate(Route.GymWorkout(it)) },
                        onNavigateToCreateWorkout = { navigate(Route.CreateSplit) }
                    )
                }
            }

            composable<Route.GymWorkout> {
                GymWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    releaseNavigationGuard = ::releaseNavigationGuard,
                    showNavigationGuard = showNavigationGuard,
                    onShowNavigationGuardChange = { showNavigationGuard = it }
                )
            }

            composable<Route.CreateSplit> {
                CreateGymWorkoutScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    onGuardReleased = ::releaseNavigationGuard,
                    showNavigationGuard = showNavigationGuard,
                    onShowNavigationGuardChange = { showNavigationGuard = it }
                )
            }

            navigation<Route.Cardio>(startDestination = Route.CardioList) {
                composable<Route.CardioList> {
                    CardioListScreen(
                        onNavigateToWorkout = { navigate(Route.CardioItem(it)) },
                        onNavigateToCreateCardio = { navigate(Route.CreateCardio) }
                    )
                }
            }

            composable<Route.CardioItem> {
                CardioItemScreen(
                    onNavigateBack = ::popBackStack,
                    onNavigationGuardChange = ::onNavigationGuardChange,
                    onGuardReleased = ::releaseNavigationGuard,
                    showNavigationGuard = showNavigationGuard,
                    onShowNavigationGuardChange = { showNavigationGuard = it },
                    onNavigateToStats = { navigate(Route.CardioStats(it)) }
                )
            }
            composable<Route.CreateCardio> {
                CreateCardioScreen(
                    onNavigateBack = ::popBackStack
                )
            }

            navigation<Route.Stats>(startDestination = Route.StatsOverview) {
                composable<Route.StatsOverview> {
                    StatsOverviewScreen(
                        onNavigateBack = ::popBackStack,
                        onSessionNavigate = { id,  ->
                            /*if (type == WorkoutType.GYM) {
                                navigate(Route.GymSession(id))
                            } else {
                                navigate(Route.CardioSession(id))
                            }*/
                        },
                        onAddSessionNavigate = {_, type ->
                        },
                        onWorkoutStatsNavigate = {
                            // TODO
                            /*if (it.type == WorkoutType.GYM) {
                                navigate(Route.SplitStats(it.id))
                            } else {
                                navigate(Route.CardioStats(it.id))
                            }*/
                        }
                    )
                }
                composable<Route.GymSession> {
                    GymSessionStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.CardioSession> {
                    CardioSessionScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.SplitStats> {
                    SplitStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
                composable<Route.CardioStats> {
                    CardioStatsScreen(
                        onNavigateBack = ::popBackStack
                    )
                }
            }

            composable<Route.Info> {
                InfoScreen(
                    onNavigateBack = ::popBackStack,
                    onDeleteFinished = { navigate(Route.Welcome) }
                )
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
