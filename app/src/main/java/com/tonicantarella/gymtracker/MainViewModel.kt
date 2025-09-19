package com.tonicantarella.gymtracker

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonicantarella.gymtracker.preferences.GymPreferences
import com.tonicantarella.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = true,
    val userHasBeenWelcomed: Boolean = false,
    val initialRoute: Route = Route.Welcome
)

class MainViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkUserPreferences()
    }

    fun checkUserPreferences() {
        viewModelScope.launch {
            val hasBeenWelcomed = dataStore.data
                .map { it[GymPreferences.USER_HAS_BEEN_WELCOMED] ?: false }
                .first()

            _uiState.update {
                it.copy(
                    userHasBeenWelcomed = hasBeenWelcomed,
                    initialRoute = if (hasBeenWelcomed) Route.GymMain else it.initialRoute,
                    loading = false
                )
            }
        }
    }

    fun onUserWelcomed() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[GymPreferences.USER_HAS_BEEN_WELCOMED] = true
            }
        }
    }
}