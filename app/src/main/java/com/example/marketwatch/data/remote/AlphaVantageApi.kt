package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

@Suppress("unused")
interface AlphaVantageApi {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String
    ): QuoteResponse
}
