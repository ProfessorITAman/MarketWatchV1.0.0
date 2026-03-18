package com.example.marketwatch.domain.models

data class Quote(
    val symbol: String,
    val price: Double,
    val changePercent: Double,
    val change: String? = null,
    val latestTradingDay: String? = null,
    val name: String? = null
)
