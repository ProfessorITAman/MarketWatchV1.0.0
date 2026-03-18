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

/**
 * Репозиторий настроек приложения (SharedPreferences wrapper).
 *
 * Хранит пользовательские настройки: API ключ, интервалы обновления, тему, валюту, язык.
 * Предоставляет удобный API с геттерами/сеттерами + реактивный StateFlow для UI.
 *
 * Используется во всех слоях: SettingsScreen → ViewModel → Repository → SharedPreferences.
 *
 * Файл настроек: `/data/data/com.example/shared_prefs/marketwatch_prefs.xml`
 */
class SettingsRepository(private val context: Context) {
    /**
     * SharedPreferences с приватным режимом (только наше приложение).
     * Автоматически создает XML файл с настройками.
     */

    private val prefs by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    /**
     * Константы ключей SharedPreferences (ТЗ 3.4).
     * const val = compile-time константы для безопасности и читаемости.
     */
    // ТЗ 3.4 ключи
    companion object {
        const val PREF_API_KEY = "pref_api_key" // Alpha Vantage API ключ
        const val PREF_REFRESH_INTERVAL = "pref_refresh_interval" // 0=Off, 15, 60 мин
        const val PREF_CURRENCY = "pref_currency" // USD, EUR, RUB
        const val PREF_DARK_MODE = "pref_dark_mode" // true=темная тема
        const val PREF_LANGUAGE = "pref_language" // ru, en
    }

    // region API Key (передается в AlphaVantageService)
    /**
     * API ключ Alpha Vantage (обязательный для запросов).
     *
     * Demo ключ: "V8G6X3K7Y9P2M5N1Q4R8T2U6" [фейковый]
     * Реальный ключ получаем бесплатно: https://www.alphavantage.co/support/#api-key
     */
    var apiKey: String
        get() = prefs.getString(PREF_API_KEY, "CZJ3ZYDNQBDXQ8S6")
            ?: "CZJ3ZYDNQBDXQ8S6"
        set(value) = prefs.edit { putString(PREF_API_KEY, value) }

    // ✅ Автосохранение
    // endregion

    // region Refresh Interval (автообновление котировок)
    /**
     * Интервал автообновления котировок в минутах.
     * 0 = отключено, 15 = каждые 15 мин, 60 = каждый час.
     * Используется WorkManager для фоновых задач.
     */
    var refreshInterval: Int
        get() = prefs.getInt(PREF_REFRESH_INTERVAL, 0)
        set(value) = prefs.edit {
            putInt(PREF_REFRESH_INTERVAL, value)
            // ✅ ЭТО ВМЕСТО scheduleRefresh()
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

        // ✅ Long для WorkManager!
        val minutes = refreshInterval.coerceAtLeast(15).toLong()

        val request = PeriodicWorkRequestBuilder<QuoteRefreshWorker>(
            repeatInterval = minutes,           // Long!
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
    // endregion

    // region Currency (форматирование цен)
    /**
     * Валюта отображения цен (USD, EUR, RUB).
     * Влияет на NumberFormat и символ валюты в UI.
     */
    var currency: String
        get() = prefs.getString(PREF_CURRENCY, "USD") ?: "USD"
        set(value) = prefs.edit { putString(PREF_CURRENCY, value) }

    // endregion

    // region Dark Mode (Material You)
    /**
     * Темная тема (true=темная, false=светлая/системная).
     * Используется в AppTheme и DynamicColors.
     */
    var isDarkMode: Boolean
        get() = prefs.getBoolean(PREF_DARK_MODE, false)  // false = светлая по умолчанию
        set(value) = prefs.edit { putBoolean(PREF_DARK_MODE, value) }

    /**
     * ✅ РЕАКТИВНЫЙ StateFlow для автоматического обновления UI.
     *
     * При изменении isDarkMode → Compose автоматически перерисовывает тему.
     * callbackFlow + OnSharedPreferenceChangeListener = реактивность SharedPreferences.
     *
     * Использование: val theme by settingsRepository.isDarkModeFlow.collectAsState()
     */
    val isDarkModeFlow: StateFlow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_DARK_MODE) {
                trySend(prefs.getBoolean(PREF_DARK_MODE, false))// ✅ Отправляем новое значение
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean(PREF_DARK_MODE, false)) // начальное значение

        awaitClose { // ✅ Cleanup при завершении коллекции
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()), // ✅ IO для SharedPrefs
        started = SharingStarted.WhileSubscribed(5000), //✅ Кэшируем 5 сек после unSubscribe
        initialValue = prefs.getBoolean(PREF_DARK_MODE, false)// ✅ Начальное значение
    )

    // region Language (локализация)
    /**
     * Язык приложения (ru, en).
     * Устанавливает Locale и Context.createConfigurationContext().
     */
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