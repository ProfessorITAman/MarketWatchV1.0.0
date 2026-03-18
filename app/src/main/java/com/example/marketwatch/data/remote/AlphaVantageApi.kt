package com.example.marketwatch.data.remote

import com.example.marketwatch.data.remote.dto.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Интерфейс для работы с Alpha Vantage API (котировки акций).
 *
 * Используется Retrofit для автоматической генерации HTTP-клиента.
 * Все методы помечены suspend для асинхронного вызова из корутин (viewModelScope.launch).
 *
 * Базовый URL: https://www.alphavantage.co/
 * Документация: https://www.alphavantage.co/documentation/
 *
 * Эндпоинт: GET /query?... - универсальный запрос для всех функций API
 */
@Suppress("unused")
interface AlphaVantageApi {
    /**
     * Получить текущие котировки акции (GLOBAL_QUOTE).
     *
     * Пример запроса:
     * GET https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=AAPL&apikey=demo
     *
     * @param function тип запроса. "GLOBAL_QUOTE" = текущая цена акции [по умолчанию]
     * @param symbol тикер акции (AAPL, GOOGL, MSFT, TSLA)
     * @param apikey ваш API ключ от Alpha Vantage (бесплатно до 25 запросов/мин)
     *
     * @return QuoteResponse с данными котировок или ошибкой Retrofit
     *
     * Пример ответа:
     * {
     *   "Global Quote": {
     *     "01. symbol": "AAPL",
     *     "05. price": "150.25",
     *     "10. change percent": "2.34%"
     *   }
     * }
     */
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apikey: String
    ): QuoteResponse
}