package com.example.marketwatch.data.repository

import android.util.Log
import com.example.marketwatch.data.local.SettingsDataStore
import com.example.marketwatch.data.remote.AlphaVantageApi
import com.example.marketwatch.data.remote.dto.toQuote
import com.example.marketwatch.domain.models.Quote

class NetworkRepositoryImpl(
    private val alphaVantageApi: AlphaVantageApi,
    private val settingsDataStore: SettingsDataStore
) : NetworkRepository {

    override suspend fun getQuote(symbol: String): Quote? {
        return try {
            val apiKey = settingsDataStore.getApiKey()
            val response = alphaVantageApi.getQuote(
                function = "GLOBAL_QUOTE",
                symbol = symbol,
                apikey = apiKey
            )
            response.globalQuote?.toQuote()
        } catch (e: Exception) {
            Log.e("NetworkRepository", "API error: ${e.message}")
            null
        }
    }
}


