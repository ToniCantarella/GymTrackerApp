package com.tonicantarella.gymtracker.ui.cardio.createcardioworkout

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.repository.cardio.CardioWorkoutRepository
import com.tonicantarella.gymtracker.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCardioWorkoutUiState(
    val name: String = "",
    val unSavedChangesDialogOpen: Boolean = false,
    val confirmUnsavedChanges: Boolean = true
) {
    val hasUnsavedChanges = name.isNotEmpty()
}

class CreateCardioWorkoutViewModel(
    private val workoutRepository: CardioWorkoutRepository,
    private val dataStore: DataStore<Preferences>,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCardioWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        registerNavigationGuard()
        registerNavigationAttempts()
        getPreferences()
    }

    fun onNavigateBack() {
        navigator.popBackStack()
    }

    fun onChangeName(name: String) {
        _uiState.update {
            it.copy(
                name = name
            )
        }
    }

    fun onCreateWorkoutPressed() {
        viewModelScope.launch {
            workoutRepository.addWorkout(uiState.value.name)
        }
        navigator.releaseGuard()
        navigator.popBackStack()
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update {
            it.copy(
                unSavedChangesDialogOpen = false
            )
        }
    }

    fun onConfirmUnsavedChangesDialog(doNotAskAgain: Boolean) {
        _uiState.update {
            it.copy(
                unSavedChangesDialogOpen = false
            )
        }
        if (doNotAskAgain) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] = false
                }
            }
        }
        navigator.releaseGuard()
    }

    fun getPreferences() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[GymPreferences.SHOW_UNSAVED_CHANGES_DIALOG] ?: true
            }.distinctUntilChanged().collect { showDialog ->
                _uiState.update {
                    it.copy(
                        confirmUnsavedChanges = showDialog
                    )
                }
            }
        }
    }

    private fun registerNavigationGuard() {
        viewModelScope.launch {
            uiState
                .map { it.hasUnsavedChanges }
                .distinctUntilChanged()
                .collect { hasChanges ->
                    navigator.guard(hasChanges)
                }
        }
    }

    private fun registerNavigationAttempts() {
        viewModelScope.launch {
            navigator.navigationAttempts.collect { direction ->
                if (navigator.isGuarded && uiState.value.confirmUnsavedChanges) {
                    _uiState.update {
                        it.copy(
                            unSavedChangesDialogOpen = true
                        )
                    }
                }
            }
        }
    }
}