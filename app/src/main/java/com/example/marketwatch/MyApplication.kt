package com.example.marketwatch

import android.app.Application
import androidx.work.*
import com.example.marketwatch.data.worker.QuoteRefreshWorker
import com.example.marketwatch.di.appModule
import com.example.marketwatch.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit  // ✅ КЛЮЧЕВОЙ ИМПОРТ!

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Koin DI
        startKoin {
            androidContext(this@MyApplication)
            modules(networkModule, appModule)
            //workManagerFactory()
        }

        // 2. WorkManager
        setupWorkManager()
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<QuoteRefreshWorker>(
            15, TimeUnit.MINUTES  // ✅ TimeUnit.MINUTES!
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "quote_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}



