package com.example.marketwatch.data.remote.dto

@Suppress("unused")
data class InstrumentDto(
    val symbol: String,
    val price: Double,
    val change: Double?
)