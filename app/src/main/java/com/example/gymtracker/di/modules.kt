package com.example.gymtracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.gymtracker.MainViewModel
import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.repository.AppRepository
import com.example.gymtracker.repository.AppRepositoryImpl
import com.example.gymtracker.repository.cardio.CardioSessionRepository
import com.example.gymtracker.repository.cardio.CardioSessionRepositoryImpl
import com.example.gymtracker.repository.cardio.CardioStatsRepository
import com.example.gymtracker.repository.cardio.CardioStatsRepositoryImpl
import com.example.gymtracker.repository.cardio.CardioWorkoutRepository
import com.example.gymtracker.repository.cardio.CardioWorkoutRepositoryImpl
import com.example.gymtracker.repository.gym.GymSessionRepository
import com.example.gymtracker.repository.gym.GymSessionRepositoryImpl
import com.example.gymtracker.repository.gym.GymStatsRepository
import com.example.gymtracker.repository.gym.GymStatsRepositoryImpl
import com.example.gymtracker.repository.gym.GymWorkoutRepository
import com.example.gymtracker.repository.gym.GymWorkoutRepositoryImpl
import com.example.gymtracker.ui.cardio.cardioworkout.CardioWorkoutViewModel
import com.example.gymtracker.ui.cardio.cardioworkouts.CardioWorkoutsViewModel
import com.example.gymtracker.ui.cardio.createcardioworkout.CreateCardioWorkoutViewModel
import com.example.gymtracker.ui.gym.creategymworkout.CreateGymWorkoutViewModel
import com.example.gymtracker.ui.gym.gymworkout.GymWorkoutViewModel
import com.example.gymtracker.ui.gym.gymworkouts.GymWorkoutsViewModel
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
            .build()
    }

    single { get<GymDatabase>().gymWorkoutDao() }
    single { get<GymDatabase>().cardioWorkoutDao() }
    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().setDao() }
    single { get<GymDatabase>().setSessionDao() }
    single { get<GymDatabase>().gymSessionDao() }
    single { get<GymDatabase>().cardioDao() }
    single { get<GymDatabase>().cardioSessionDao() }
}

val repositoryModule = module {
    single<AppRepository> { AppRepositoryImpl(get()) }

    single<GymWorkoutRepository> {
        GymWorkoutRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
    single<GymSessionRepository> {
        GymSessionRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<GymStatsRepository> {
        GymStatsRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single<CardioWorkoutRepository> {
        CardioWorkoutRepositoryImpl(
            get(),
            get()
        )
    }
    single<CardioSessionRepository> {
        CardioSessionRepositoryImpl(
            get(),
            get()
        )
    }
    single<CardioStatsRepository> {
        CardioStatsRepositoryImpl(
            get(),
            get()
        )
    }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::GymWorkoutsViewModel)
    viewModelOf(::CardioWorkoutsViewModel)
    viewModelOf(::CardioWorkoutViewModel)
    viewModelOf(::GymWorkoutViewModel)
    viewModelOf(::CreateGymWorkoutViewModel)
    viewModelOf(::CreateCardioWorkoutViewModel)
    viewModelOf(::StatsOverviewViewModel)
    viewModelOf(::GymSessionViewModel)
    viewModelOf(::CardioSessionViewModel)
    viewModelOf(::SplitStatsViewModel)
    viewModelOf(::CardioStatsViewModel)
    viewModelOf(::InfoViewModel)
}