package com.example.gymtracker.ui.info

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.database.repository.WorkoutRepository
import kotlinx.coroutines.launch

class InfoViewModel(
    private val workoutRepository: WorkoutRepository,
    private val dataStore: DataStore<Preferences>
): ViewModel() {

    fun onDeleteAllData(onDeleteFinished: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.deleteAllData()
            dataStore.edit { it.clear() }
            onDeleteFinished()
        }
    }
}