package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import com.example.marketwatch.data.remote.dto.TimeSeriesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Базовый интерфейс для сервисов API.
 *
 * Определяет контракт для всех сервисов котировок акций.
 * Позволяет легко переключаться между Alpha Vantage ↔ CoinGecko ↔ другой API.
 *
 * Каждый имплементирующий класс (AlphaVantageService) обязан предоставить:
 * - suspend метод getQuote() для получения котировок
 * - автоматическую обработку JSON → GlobalQuote через Retrofit + Gson
 */
interface ApiService {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("market") targetCurrency: String = "USD",
        @Query("apikey") apikey: String
    ): QuoteResponse

    @GET("query")
    suspend fun getCryptoQuote(
        @Query("function") function: String = "DIGITAL_CURRENCY_DAILY",
        @Query("symbol") symbol: String,
        @Query("market") targetCurrency: String = "USD",
        @Query("apikey") apikey: String
    ): QuoteResponse

    @GET("query")
    suspend fun getTimeSeries(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("market") targetCurrency: String = "USD",
        @Query("apikey") apikey: String
    ): TimeSeriesResponse

    data class TimeSeriesData(val open: Double, val close: Double)

    /**
     * Получить котировки акции по тикеру.
     *
     * @param symbol тикер акции (AAPL, GOOGL, MSFT)
     * @return QuoteResponse с распарсенными данными GlobalQuote
     * @throws HttpException, IOException при ошибках сети/API
     */
}

