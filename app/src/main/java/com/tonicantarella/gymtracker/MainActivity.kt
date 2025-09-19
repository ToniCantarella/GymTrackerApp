package com.tonicantarella.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.theme.GymTrackerTheme
import com.tonicantarella.gymtracker.update.InAppUpdateHandler
import com.tonicantarella.gymtracker.update.UpdateDialog
import com.tonicantarella.gymtracker.update.UpdateStatus
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    private val navigator: Navigator by inject()
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
                        viewModel = viewModel,
                        navigator = navigator
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