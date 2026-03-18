package com.example.marketwatch.domain.repository

import com.example.marketwatch.data.local.InstrumentDao
import com.example.marketwatch.data.local.InstrumentEntity
import com.example.marketwatch.data.remote.ApiService
import com.example.marketwatch.presentaion.viewmodels.util.NetworkChecker
import com.example.marketwatch.presentaion.viewmodels.util.NoNetworkException
import kotlinx.coroutines.flow.Flow

class InstrumentRepository(
    private val apiService: ApiService,  // ← ТВОЙ существующий ApiService!
    private val dao: InstrumentDao,
    private val networkChecker: NetworkChecker,
) {
    fun observeInstrument(symbol: String): Flow<InstrumentEntity?> =
        dao.observeInstrument(symbol)

    suspend fun refreshInstrument(symbol: String) {
        if (!networkChecker.isNetworkAvailable()) {
            throw NoNetworkException()
        }

        // ✅ Используем ТВОЙ apiService.getQuote()!
       // val quote = apiService.getQuote(symbol).globalQuote

        val quote = apiService.getQuote(
            symbol = symbol,  // ✅ ПЕРЕДАЙ symbol!
            apikey = "CZJ3ZYDNQBDXQ8S6"  // ✅ ХАРДКОД!
        )

        val entity = InstrumentEntity(
            symbol = quote.globalQuote?.symbol ?: symbol,  // "01. symbol"
            price = quote.globalQuote?.price?.toDoubleOrNull() ?: 0.0,  // "05. price"
            change = quote.globalQuote?.changePercent
                ?.replace("%", "")
                ?.replace("+", "")
                ?.toDoubleOrNull(),
            lastUpdateTime = System.currentTimeMillis()
        )
        dao.upsert(entity)
    }
}