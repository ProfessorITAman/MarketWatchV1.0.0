package com.example.marketwatch.data.remote.dto

import com.example.marketwatch.domain.models.Quote
import com.google.gson.annotations.SerializedName

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("09. change") val change: String,
    @SerializedName("10. change percent") val changePercent: String,
    @SerializedName("07. latest trading day") val latestTradingDay: String
)

fun GlobalQuote.toQuote(): Quote = Quote(
    symbol = symbol,
    price = price.toDoubleOrNull() ?: 0.0,
    changePercent = changePercent.removeSuffix("%").toDoubleOrNull() ?: 0.0,
    change = change,
    latestTradingDay = latestTradingDay
)