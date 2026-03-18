package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import com.example.marketwatch.data.remote.dto.TimeSeriesResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

/**
 * Сервис для работы с Alpha Vantage API (котировки акций).
 *
 * Реализует паттерн Repository/Service Layer в MVVM архитектуре.
 * Инкапсулирует создание Retrofit клиента и логику HTTP-запросов.
 * Переиспользует кастомный OkHttpClient (с логгером, таймаутами,
 * кэшированием, перехватчиками).
 *
 * Преимущества:
 * - Единая точка доступа к API
 * - Легко мокать для тестов (Dependency Injection)
 * - Скрывает детали Retrofit от ViewModel
 *
 * @param okHttpClient настроенный HTTP-клиент с логгером, таймаутами,
 *                     интерцепторами (авторизация, ретраи, кэш)
 * @see ApiService базовый интерфейс сервиса
 */

abstract class AlphaVantageService(
    okHttpClient: OkHttpClient // ✅ Инжектируется извне (DI: Hilt/Dagger/Koin)
) : ApiService { // Реализует контракт для разных API сервисов

    /**
     * Retrofit клиент, настроенный для Alpha Vantage.
     *
     * Ключевые настройки:
     * - baseUrl = https://www.alphavantage.co/
     * - OkHttpClient с логами, таймаутами (30s), кэшем
     * - GsonConverter для парсинга JSON → QuoteResponse/GlobalQuote
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.alphavantage.co/") // ✅ Базовый URL API
        .client(okHttpClient) // ✅ Кастомный HTTP клиент
        .addConverterFactory(GsonConverterFactory.create()) // ✅ JSON → Kotlin объекты
        .build()

    /**
     * Синглтон API интерфейс (ленивая инициализация).
     * Retrofit.create() генерирует прокси для HTTP вызовов.
     */
    private val api = retrofit.create(ApiService::class.java)  // ← ApiService!

    // ✅ Реализация абстрактных функций:
    override suspend fun getQuote(
        function: String,
        symbol: String,
        targetCurrency: String,
        apikey: String
    ): QuoteResponse {
        return api.getQuote(function, symbol, targetCurrency, apikey)  // ← api!
    }

    override suspend fun getCryptoQuote(
        function: String,
        symbol: String,
        targetCurrency: String,
        apikey: String
    ): QuoteResponse {
        return api.getCryptoQuote(function, symbol, targetCurrency, apikey)
    }

    override suspend fun getTimeSeries(
        function: String,
        symbol: String,
        targetCurrency: String,
        apikey: String
    ): TimeSeriesResponse {
        return api.getTimeSeries(function, symbol, targetCurrency, apikey)  // ✅ Правильно!
    }

    /**
     * Получить текущие котировки акции (GLOBAL_QUOTE).
     *
     * Выполняет suspend запрос к API и возвращает распарсенные данные.
     * API ключ берется из BuildConfig (gradle.properties → local.properties).
     *
     * @param symbol тикер акции (AAPL, GOOGL, MSFT, TSLA)
     * @return QuoteResponse с данными котировок
     * @throws HttpException при 4xx/5xx ошибках
     * @throws IOException при сетевых ошибках
     *
     * Лимиты API: 25 запросов/мин (бесплатный тариф)
     */
    //override suspend fun getQuote(symbol: String): QuoteResponse {
    //    return api.getQuote(
    //        function = "GLOBAL_QUOTE", // ✅ Текущая цена акции
    //        symbol = symbol,  // ✅ Тикер (обязательный)
    //        apikey = BuildConfig.ALPHA_VANTAGE_API_KEY  // ✅ API ключ из BuildConfig
    //    )
  //  }
}