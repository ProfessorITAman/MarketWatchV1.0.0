package com.example.marketwatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuoteResponse(

    @SerializedName("Global Quote")
    val globalQuote: GlobalQuote? = null
)
