package com.tonicantarella.gymtracker

import android.app.Application
import com.tonicantarella.gymtracker.di.appModule
import com.tonicantarella.gymtracker.di.databaseModule
import com.tonicantarella.gymtracker.di.repositoryModule
import com.tonicantarella.gymtracker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    appModule,
                    databaseModule,
                    repositoryModule,
                    viewModelModule
                )
            )
        }
    }
}