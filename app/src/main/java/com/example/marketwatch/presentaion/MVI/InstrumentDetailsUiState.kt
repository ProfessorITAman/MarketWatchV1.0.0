package com.example.marketwatch.presentaion.MVI

data class InstrumentDetailsUiState(
    val symbol: String = "",
    val price: String = "",
    val change: String = "",
    val lastUpdateTime: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)