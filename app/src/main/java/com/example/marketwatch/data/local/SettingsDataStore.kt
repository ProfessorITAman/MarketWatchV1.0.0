package com.example.marketwatch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

class SettingsDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    }

    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val refreshIntervalKey = intPreferencesKey("refresh_interval")
    private val currencyKey = stringPreferencesKey("currency")
    private val languageKey = stringPreferencesKey("language")
    private val apiKeyKey = stringPreferencesKey("api_key")

    //Flow
    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[darkModeKey] ?: false
        }

    val languageFlow: StateFlow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[languageKey] ?: "ru" }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "ru"
        )

    val isDarkModeStateFlow: StateFlow<Boolean> = isDarkModeFlow  // Уже есть!
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val refreshIntervalFlow: StateFlow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[refreshIntervalKey] ?: 0 }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val currencyFlow: StateFlow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[currencyKey] ?: "USD" }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "USD"
        )



    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { settings ->
            settings[darkModeKey] = isDark
        }
    }


    suspend fun updateRefreshInterval(interval: Int) {
        context.dataStore.edit { settings ->
            settings[refreshIntervalKey] = interval
        }
    }


    suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { settings ->
            settings[currencyKey] = currency
        }
    }


    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { settings ->
            settings[languageKey] = language
        }
    }


    suspend fun updateApiKey(apiKey: String) {
        context.dataStore.edit { settings ->
            settings[apiKeyKey] = apiKey
        }
    }

    // Синхронное чтение (для Worker)
    suspend fun getApiKey(): String {
        return context.dataStore.data
            .map { preferences ->
                preferences[apiKeyKey] ?: ""
            }.first()
    }
}
