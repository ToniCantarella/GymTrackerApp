package com.example.gymtracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.gymtracker.MainViewModel
import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.ui.cardio.cardiolist.CardioListViewModel
import com.example.gymtracker.ui.stats.StatsOverviewViewModel
import com.example.gymtracker.ui.workouts.createsplit.CreateSplitViewModel
import com.example.gymtracker.ui.workouts.split.SplitViewModel
import com.example.gymtracker.ui.workouts.splitslist.SplitListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            File(androidContext().filesDir, "settings.preferences_pb")
        }
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            GymDatabase::class.java,
            "gym-tracker-db"
        )
            .build()
    }

    single { get<GymDatabase>().splitDao() }
    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().setDao() }
    single { get<GymDatabase>().setSessionDao() }
    single { get<GymDatabase>().sessionDao() }

    single {
        WorkoutRepository(
            splitDao = get(),
            exerciseDao = get(),
            setDao = get(),
            setSessionDao = get(),
            sessionDao = get()
        )
    }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::SplitListViewModel)
    viewModelOf(::CardioListViewModel)
    viewModelOf(::SplitViewModel)
    viewModelOf(::CreateSplitViewModel)
    viewModelOf(::StatsOverviewViewModel)
}