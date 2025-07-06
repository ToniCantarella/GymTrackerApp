package com.example.gymtracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.example.gymtracker.MainViewModel
import com.example.gymtracker.ui.cardio.cardiolist.CardioListViewModel
import com.example.gymtracker.ui.workouts.split.SplitViewModel
import com.example.gymtracker.ui.workouts.splitslist.SplitListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            File(androidContext().filesDir, "settings.preferences_pb")
        }
    }

    viewModel { MainViewModel(get()) }
}

val viewModelModule = module {
    viewModel { (handle: SavedStateHandle) -> SplitViewModel(handle) }
    viewModel { SplitListViewModel() }
    viewModel { CardioListViewModel() }
}