package com.example.gymtracker.di

import androidx.lifecycle.SavedStateHandle
import com.example.gymtracker.ui.workouts.split.SplitViewModel
import com.example.gymtracker.ui.workouts.splitslist.SplitListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

}

val viewModelModule = module {
    viewModel { (handle: SavedStateHandle) -> SplitViewModel(handle) }
    viewModel { SplitListViewModel() }
}