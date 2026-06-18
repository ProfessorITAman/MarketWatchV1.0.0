package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import com.example.marketwatch.data.remote.dto.TimeSeriesResponse
import retrofit2.http.GET
import retrofit2.http.Query

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
}

