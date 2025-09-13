package com.example.gymtracker.update

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UpdateUiState(
    val updateStatus: UpdateStatus = UpdateStatus.IDLE,
    val progress: Float = 0f
)

enum class UpdateStatus {
    IDLE,
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    CANCELED,
    FAILED,
    INSTALLED,
    UNKNOWN
}

class InAppUpdateHandler(
    private val appUpdateManager: AppUpdateManager
) {
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private lateinit var updateLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val listener = InstallStateUpdatedListener { updateState ->
        when (updateState.installStatus()) {

            InstallStatus.DOWNLOADED -> {
                _uiState.update { it.copy(updateStatus = UpdateStatus.DOWNLOADED) }
            }

            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = updateState.bytesDownloaded()
                val totalBytesToDownload = updateState.totalBytesToDownload()
                _uiState.update {
                    it.copy(
                        updateStatus = UpdateStatus.DOWNLOADING,
                        progress = bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()
                    )
                }
            }

            InstallStatus.CANCELED -> {
                _uiState.update { it.copy(updateStatus = UpdateStatus.CANCELED) }
            }

            InstallStatus.FAILED -> {
                _uiState.update { it.copy(updateStatus = UpdateStatus.FAILED) }
            }

            InstallStatus.INSTALLED -> {
                _uiState.update { it.copy(updateStatus = UpdateStatus.INSTALLED) }
            }

            else -> {
                _uiState.update { it.copy(updateStatus = UpdateStatus.UNKNOWN) }
            }
        }
    }

    fun registerUpdateLauncher(activity: ComponentActivity) {
        updateLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        _uiState.update {
                            it.copy(updateStatus = UpdateStatus.DOWNLOADING) }
                    }

                    RESULT_CANCELED -> {
                        _uiState.update { it.copy(updateStatus = UpdateStatus.CANCELED) }
                    }

                    else -> {
                        _uiState.update { it.copy(updateStatus = UpdateStatus.UNKNOWN) }
                    }
                }
            }
    }

    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                _uiState.update { it.copy(updateStatus = UpdateStatus.AVAILABLE) }
            }
        }
    }

    fun startUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            val priority = updateInfo.updatePriority()
            if (priority == 5 && updateInfo.isImmediateUpdateAllowed) {
                val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                updateLauncher.let { launcher ->
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo,
                        launcher,
                        options
                    )
                }
            } else {
                val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                monitorFlexibleUpdate()
                updateLauncher.let { launcher ->
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo,
                        launcher,
                        options
                    )
                }
            }
        }
    }

    fun disposeFlexibleUpdate() {
        appUpdateManager.unregisterListener(listener)
    }

    fun resumeUpdateIfNeeded() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
            }
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                _uiState.update { it.copy(updateStatus = UpdateStatus.DOWNLOADED) }
            }
        }
    }

    fun dismissUpdate() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.IDLE) }
    }

    private fun monitorFlexibleUpdate() {
        appUpdateManager.registerListener(listener)
    }
}