package com.example.marketwatch.di

import androidx.work.WorkManager
import com.example.marketwatch.data.local.InstrumentDao
import com.example.marketwatch.data.remote.ApiService
import com.example.marketwatch.data.local.AppDatabase
import com.example.marketwatch.data.local.SettingsDataStore
import com.example.marketwatch.data.local.FavoriteDao
import com.example.marketwatch.data.remote.AlphaVantageApi
import com.example.marketwatch.presentaion.viewmodels.InstrumentDetailsViewModel
import com.example.marketwatch.presentaion.viewmodels.util.NetworkChecker
import com.example.marketwatch.domain.repository.InstrumentRepository
import com.example.marketwatch.data.repository.NetworkRepository
import com.example.marketwatch.data.repository.NetworkRepositoryImpl
import com.example.marketwatch.data.repository.SettingsRepository
import com.example.marketwatch.data.worker.QuoteRefreshWorker
import com.example.marketwatch.presentaion.viewmodels.FavoritesViewModel
import com.example.marketwatch.presentaion.MVI.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModule = module {
    includes(networkModule)

    // 1. Room Database
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().favoriteDao() }
    single<InstrumentDao> { get<AppDatabase>().instrumentDao() }

    // 2. Settings
    single { SettingsRepository(androidContext()) }
    single { SettingsDataStore(get()) }
    factory { NetworkChecker(androidContext()) }

    // 3. Repositories
    single<NetworkRepository> {
        NetworkRepositoryImpl(
            alphaVantageApi = get<AlphaVantageApi>(),
            settingsDataStore = get()
        )
    }

    single {
        InstrumentRepository(
            get<ApiService>(),
            get<InstrumentDao>(),
            get<NetworkChecker>()
        )
    }

    // 4. ViewModels - ✅ ПРАВИЛЬНЫЙ синтаксис!
    viewModel {
        SearchViewModel(
            get<ApiService>(),
            get<FavoriteDao>()
        )
    }

    viewModel {
        FavoritesViewModel(
            get<FavoriteDao>(),
            get<NetworkRepository>()
        )
    }

    viewModel {
        InstrumentDetailsViewModel(
            get<NetworkRepository>()
        )
    }

    single { WorkManager.getInstance(androidContext()) }

    //worker {
    //    QuoteRefreshWorker(
    //        context = androidContext(),
   //         params = get(),
    //        favoriteDao = get(),
    //        networkRepo = get()
    //    )
   // }
}




