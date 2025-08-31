package com.example.gymtracker.ui.info

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.GymPreferences
import com.example.gymtracker.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class InfoUiState(
    val showConfirmOnFinishWorkout: Boolean = true
)

class InfoViewModel(
    private val appRepository: AppRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val showFinishWorkoutDialog = dataStore.data
                .map { it[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] ?: true }
                .first()

            _uiState.value = InfoUiState(
                showConfirmOnFinishWorkout = showFinishWorkoutDialog
            )
        }
    }

    fun onShowFinishDialogChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] = checked
            }
            _uiState.value = InfoUiState(
                showConfirmOnFinishWorkout = checked
            )
        }
    }

    fun onDeleteAllData(onDeleteFinished: () -> Unit) {
        viewModelScope.launch {
            appRepository.deleteAllData()
            dataStore.edit { it.clear() }
            onDeleteFinished()
        }
    }
}