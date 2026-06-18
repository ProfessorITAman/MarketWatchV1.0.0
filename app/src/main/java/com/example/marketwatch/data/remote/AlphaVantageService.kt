package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import com.example.marketwatch.data.remote.dto.TimeSeriesResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

abstract class AlphaVantageService(
    okHttpClient: OkHttpClient 
) : ApiService { 
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.alphavantage.co/") 
        .client(okHttpClient) 
        .addConverterFactory(GsonConverterFactory.create()) 
        .build()


    private val api = retrofit.create(ApiService::class.java)  

    override suspend fun getQuote(
        function: String,
        symbol: String,
        targetCurrency: String,
        apikey: String
    ): QuoteResponse {
        return api.getQuote(function, symbol, targetCurrency, apikey)  
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
        return api.getTimeSeries(function, symbol, targetCurrency, apikey)  
    }
}
