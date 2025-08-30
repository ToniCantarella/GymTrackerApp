package com.example.gymtracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.gymtracker.MainViewModel
import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.database.MIGRATION_5_6
import com.example.gymtracker.database.MIGRATION_6_7
import com.example.gymtracker.database.MIGRATION_7_8
import com.example.gymtracker.database.repository.CardioRepository
import com.example.gymtracker.database.repository.CardioRepositoryImpl
import com.example.gymtracker.database.repository.GymRepository
import com.example.gymtracker.database.repository.GymRepositoryImpl
import com.example.gymtracker.database.repository.StatRepository
import com.example.gymtracker.database.repository.StatRepositoryImpl
import com.example.gymtracker.database.repository.WorkoutRepository
import com.example.gymtracker.database.repository.WorkoutRepositoryImpl
import com.example.gymtracker.ui.cardio.cardioitem.CardioItemViewModel
import com.example.gymtracker.ui.cardio.cardiolist.CardioListViewModel
import com.example.gymtracker.ui.cardio.createcardio.CreateCardioViewModel
import com.example.gymtracker.ui.gym.createsplit.CreateSplitViewModel
import com.example.gymtracker.ui.gym.split.SplitViewModel
import com.example.gymtracker.ui.gym.splitslist.SplitListViewModel
import com.example.gymtracker.ui.info.InfoViewModel
import com.example.gymtracker.ui.stats.cardio.CardioSessionViewModel
import com.example.gymtracker.ui.stats.cardio.CardioStatsViewModel
import com.example.gymtracker.ui.stats.gym.GymSessionViewModel
import com.example.gymtracker.ui.stats.gym.SplitStatsViewModel
import com.example.gymtracker.ui.stats.overview.StatsOverviewViewModel
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
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
            .build()
    }

    single { get<GymDatabase>().gymWorkoutPlanDao() }
    single { get<GymDatabase>().cardioWorkoutPlanDao() }
    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().setDao() }
    single { get<GymDatabase>().setSessionDao() }
    single { get<GymDatabase>().gymSessionDao() }
    single { get<GymDatabase>().cardioDao() }
    single { get<GymDatabase>().cardioSessionDao() }

    single<WorkoutRepository> {
        WorkoutRepositoryImpl(
            gymWorkoutDao = get(),
            cardioWorkoutDao = get(),
            gymSessionDao = get(),
            cardioMetricsDao = get(),
            cardioSessionDao = get()
        )
    }
    single<GymRepository> {
        GymRepositoryImpl(
            workoutDao = get(),
            exerciseDao = get(),
            setDao = get(),
            setSessionDao = get(),
            gymSessionDao = get()
        )
    }
    single<CardioRepository> {
        CardioRepositoryImpl(
            cardioWorkoutDao = get(),
            cardioMetricsDao = get(),
            cardioSessionDao = get()
        )
    }
    single<StatRepository> {
        StatRepositoryImpl(
            db = get(),
            gymWorkoutDao = get(),
            cardioWorkoutDao = get(),
            exerciseDao = get(),
            setDao = get(),
            setSessionDao = get(),
            gymSessionDao = get(),
            cardioMetricsDao = get(),
            cardioSessionDao = get()
        )
    }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::SplitListViewModel)
    viewModelOf(::CardioListViewModel)
    viewModelOf(::CardioItemViewModel)
    viewModelOf(::SplitViewModel)
    viewModelOf(::CreateSplitViewModel)
    viewModelOf(::CreateCardioViewModel)
    viewModelOf(::StatsOverviewViewModel)
    viewModelOf(::GymSessionViewModel)
    viewModelOf(::CardioSessionViewModel)
    viewModelOf(::SplitStatsViewModel)
    viewModelOf(::CardioStatsViewModel)
    viewModelOf(::InfoViewModel)
}