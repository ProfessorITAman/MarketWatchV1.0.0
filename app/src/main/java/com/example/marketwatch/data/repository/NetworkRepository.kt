package com.example.marketwatch.data.repository

import com.example.marketwatch.data.remote.dto.GlobalQuote
import com.example.marketwatch.data.remote.ApiService
import com.example.marketwatch.domain.models.Quote
import kotlinx.coroutines.delay


//class NetworkRepository(
//    private val apiService: ApiService,
//    private val settingsRepo: SettingsRepository
//) {
 //   suspend fun getQuote(symbol: String): GlobalQuote? {
 //       val language = settingsRepo.language
  //      val currency = settingsRepo.currency
  //      delay(500)
   //     return try {
  //          // ✅ ИСПРАВЬ ЭТУ СТРОКУ:
  //          apiService.getQuote(
  //              symbol = symbol,  // ← ДОБАВЬ!
  //              apikey = "CZJ3ZYDNQBDXQ8S6"  // ← ДОБАВЬ!
   //         ).globalQuote
   //     } catch (_: Exception) {
   //         mockQuote(symbol, currency, language)
    //    }
   // }

interface NetworkRepository {
    suspend fun getQuote(symbol: String): Quote?
}
    private fun mockQuote(symbol: String, currency: String, language: String): GlobalQuote {
        val basePrice = 264.58

        // ✅ Конвертация по языку/валюте
        val displayPrice = when (currency) {
            "RUB" -> "${(basePrice * 95).format(0)} ₽"   // 25135 ₽
            "EUR" -> "${(basePrice * 0.92).format(2)} €" // 243.41 €
            else -> "${basePrice.format(2)} $"           // 264.58 $
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
// Extension для форматирования
private fun Double.format(decimals: Int): String =
    "%.${decimals}f".format(this)


