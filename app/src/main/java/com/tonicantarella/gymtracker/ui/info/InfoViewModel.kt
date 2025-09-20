package com.tonicantarella.gymtracker.ui.info

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.repository.AppRepository
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class InfoUiState(
    val confirmFinishWorkout: Boolean = true,
    val confirmUnsavedChanges: Boolean = true
)

class InfoViewModel(
    private val appRepository: AppRepository,
    private val dataStore: DataStore<Preferences>,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val confirmFinishWorkout = dataStore.data
                .map { it[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] ?: true }
                .first()

            val confirmUnsavedChanges = dataStore.data
                .map { it[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] ?: true }
                .first()

            _uiState.value = InfoUiState(
                confirmFinishWorkout = confirmFinishWorkout,
                confirmUnsavedChanges = confirmUnsavedChanges
            )
        }
    }

    fun onShowFinishDialogChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[GymPreferences.SHOW_FINISH_WORKOUT_CONFIRM_DIALOG] = checked
            }
            _uiState.value = InfoUiState(
                confirmFinishWorkout = checked
            )
        }
    }

    fun onDeleteAllData() {
        viewModelScope.launch {
            appRepository.deleteAllData()
            dataStore.edit { it.clear() }
        }
        navigator.navigate(Route.Welcome)
    }
}