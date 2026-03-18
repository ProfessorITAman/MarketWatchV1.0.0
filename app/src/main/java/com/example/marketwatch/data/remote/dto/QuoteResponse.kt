package com.example.marketwatch.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Модель данных для парсинга JSON-ответа от API котировок акций (Alpha Vantage).
 *
 * Используется Retrofit + Gson для автоматической десериализации ответа API.
 * API возвращает JSON со структурой {"Global Quote": {...}}, где содержатся данные
 * о текущей цене акции, объеме торгов, временных метках и других параметрах.
 *
 * @property globalQuote объект с котировками акции (цена, объем, символ и т.д.)
 */
data class QuoteResponse(
    /**
     * Аннотация @SerializedName мапит JSON-поле "Global Quote" (с пробелом)
     * на Kotlin-свойство globalQuote. Без нее Gson не распознает поле из-за
     * пробела в названии JSON-ключа.
     *
     * Содержит все данные о котировке:
     * - 01. symbol - тикер акции (AAPL, GOOGL)
     * - 05. price - текущая цена
     * - 07. latest trading day - дата последней сделки
     * - 08. previous close - цена закрытия предыдущего дня
     * - 09. change - изменение цены
     * - 10. change percent - процентное изменение
     */
    @SerializedName("Global Quote")
    val globalQuote: GlobalQuote? = null
)