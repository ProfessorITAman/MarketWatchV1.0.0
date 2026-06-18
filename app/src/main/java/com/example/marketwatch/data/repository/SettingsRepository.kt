package com.example.marketwatch.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marketwatch.data.worker.QuoteRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsRepository(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }
    companion object {
        const val PREF_API_KEY = "pref_api_key" 
        const val PREF_REFRESH_INTERVAL = "pref_refresh_interval"
        const val PREF_CURRENCY = "pref_currency" 
        const val PREF_DARK_MODE = "pref_dark_mode"
        const val PREF_LANGUAGE = "pref_language" 
    }

    var apiKey: String
        get() = prefs.getString(PREF_API_KEY, "CZJ3ZYDNQBDXQ8S6")
            ?: "CZJ3ZYDNQBDXQ8S6"
        set(value) = prefs.edit { putString(PREF_API_KEY, value) }

    var refreshInterval: Int
        get() = prefs.getInt(PREF_REFRESH_INTERVAL, 0)
        set(value) = prefs.edit {
            putInt(PREF_REFRESH_INTERVAL, value)
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                scheduleRefresh()
            }
        }


    fun scheduleRefresh() {
        val workManager = WorkManager.getInstance(context)

        if (refreshInterval == 0) {
            workManager.cancelUniqueWork("quote_refresh")
            return
        }

        val minutes = refreshInterval.coerceAtLeast(15).toLong()

        val request = PeriodicWorkRequestBuilder<QuoteRefreshWorker>(
            repeatInterval = minutes,          
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "quote_refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    var currency: String
        get() = prefs.getString(PREF_CURRENCY, "USD") ?: "USD"
        set(value) = prefs.edit { putString(PREF_CURRENCY, value) }
    var isDarkMode: Boolean
        get() = prefs.getBoolean(PREF_DARK_MODE, false) 
        set(value) = prefs.edit { putBoolean(PREF_DARK_MODE, value) }

    val isDarkModeFlow: StateFlow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_DARK_MODE) {
                trySend(prefs.getBoolean(PREF_DARK_MODE, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean(PREF_DARK_MODE, false))

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = prefs.getBoolean(PREF_DARK_MODE, false)
    )

    var language: String
        get() = prefs.getString(PREF_LANGUAGE, "ru") ?: "ru"
        set(value) {
            prefs.edit {
                putString(PREF_LANGUAGE, value)
            }
        }
    val languageFlow: StateFlow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_LANGUAGE) {
                trySend(prefs.getString(PREF_LANGUAGE, "ru") ?: "ru")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(PREF_LANGUAGE, "ru") ?: "ru")

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ru"
    )
}
