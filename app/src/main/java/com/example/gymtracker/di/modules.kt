package com.example.gymtracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.gymtracker.MainViewModel
import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.repository.AppRepository
import com.example.gymtracker.repository.AppRepositoryImpl
import com.example.gymtracker.repository.StatsOverviewRepository
import com.example.gymtracker.repository.StatsOverviewRepositoryImpl
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
import com.example.gymtracker.ui.stats.cardio.CardioSessionStatsViewModel
import com.example.gymtracker.ui.stats.cardio.CardioWorkoutStatsViewModel
import com.example.gymtracker.ui.stats.gym.GymSessionStatsViewModel
import com.example.gymtracker.ui.stats.gym.GymWorkoutStatsViewModel
import com.example.gymtracker.ui.stats.overview.StatsOverviewViewModel
import com.example.gymtracker.update.InAppUpdateHandler
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            File(androidContext().filesDir, "settings.preferences_pb")
        }
    }
    single <AppUpdateManager>{ AppUpdateManagerFactory.create(androidContext()) }
    singleOf(::InAppUpdateHandler)
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
    singleOf(::AppRepositoryImpl) { bind<AppRepository>() }
    singleOf(::GymWorkoutRepositoryImpl) { bind<GymWorkoutRepository>() }
    singleOf(::GymSessionRepositoryImpl) { bind<GymSessionRepository>() }
    singleOf(::GymStatsRepositoryImpl) { bind<GymStatsRepository>() }
    singleOf(::CardioWorkoutRepositoryImpl) { bind<CardioWorkoutRepository>() }
    singleOf(::CardioSessionRepositoryImpl) { bind<CardioSessionRepository>() }
    singleOf(::CardioStatsRepositoryImpl) { bind<CardioStatsRepository>() }
    singleOf(::StatsOverviewRepositoryImpl) { bind<StatsOverviewRepository>() }
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
    viewModelOf(::GymSessionStatsViewModel)
    viewModelOf(::CardioSessionStatsViewModel)
    viewModelOf(::GymWorkoutStatsViewModel)
    viewModelOf(::CardioWorkoutStatsViewModel)
    viewModelOf(::InfoViewModel)
}