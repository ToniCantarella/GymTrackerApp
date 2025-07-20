package com.example.gymtracker

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val USER_HAS_BEEN_WELCOMED = booleanPreferencesKey("user_has_been_welcomed")

data class AppUiState(
    val loading: Boolean = true,
    val userHasBeenWelcomed: Boolean = false,
    val initialRoute: Route = Route.Welcome
)

class MainViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data
                .map { preferences -> preferences[USER_HAS_BEEN_WELCOMED] ?: false }
                .collect { value ->
                    _uiState.update {
                        it.copy(
                            userHasBeenWelcomed = value,
                            initialRoute = if (value) Route.Gym else it.initialRoute,
                            loading = false
                        )
                    }
                }
        }
    }

    fun onUserWelcomed() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[USER_HAS_BEEN_WELCOMED] = true
            }
        }
    }
}