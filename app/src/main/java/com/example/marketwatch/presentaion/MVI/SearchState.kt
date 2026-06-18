package com.example.marketwatch.presentaion.MVI

import com.example.marketwatch.data.remote.dto.GlobalQuote

data class SearchState(
    val quote: GlobalQuote? = null,

    val isLoading: Boolean = false,

    val error: String? = null,

    val ticker: String = ""
)
