package com.example.marketwatch.data.repository

import com.example.marketwatch.data.remote.dto.GlobalQuote
import com.example.marketwatch.data.remote.ApiService
import com.example.marketwatch.domain.models.Quote
import kotlinx.coroutines.delay

interface NetworkRepository {
    suspend fun getQuote(symbol: String): Quote?
}
    private fun mockQuote(symbol: String, currency: String, language: String): GlobalQuote {
        val basePrice = 264.58

        val displayPrice = when (currency) {
            "RUB" -> "${(basePrice * 95).format(0)} ₽"   
            "EUR" -> "${(basePrice * 0.92).format(2)} €" 
            else -> "${basePrice.format(2)} $"           
        }

        val displayChange = when (language) {
            "ru" -> "+1.54%"
            else -> "+1.54%"
        }

        return GlobalQuote(
            symbol = symbol,
            price = displayPrice,
            change = "4.00",
            changePercent = displayChange,
            latestTradingDay = "2026-02-20"
        )
    }
private fun Double.format(decimals: Int): String =
    "%.${decimals}f".format(this)


